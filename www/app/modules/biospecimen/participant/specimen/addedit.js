
angular.module('os.biospecimen.specimen.addedit', [])
  .controller('AddEditSpecimenCtrl', function(
    $scope, $state, cp, cpr, visit, specimen, extensionCtxt, hasDict, sysDict, cpDict, layout,
    CpConfigSvc, Util, ParticipantSpecimensViewState, Specimen, CollectSpecimensSvc) {

    var inputCtxts;

    function init() {
      $scope.opts = {
        cp: cp, cpr: cpr, visit: visit,
        extensionCtxt: extensionCtxt, hasDict: hasDict,
        sysDict: sysDict, cpDict: cpDict,
        editMode: !!specimen.id, reqId: specimen.reqId,
        layout: layout, mdInput: false
      }

      CpConfigSvc.getCommonCfg(cp.id, 'addSpecimen').then(
        function(cfg) {
          angular.extend($scope.opts, cfg || {});
        }
      );

      inputCtxts = $scope.inputCtxts = [
        {
          specimen: angular.copy(specimen),
          form: {},
          open: true
        }
      ]
    }

    function getState() {
      if (cp.specimenCentric) {
        return {state: $state.get('cp-specimens'), params: {cpId: cp.id}};
      } else {
        return {state: $state.get('visit-detail.overview'), params: {visitId: visit.id}};
      }
    };

    $scope.addAnother = function() {
      inputCtxts.push({
        specimen: new Specimen({lineage: 'New', visitId: visit.id, labelFmt: cpr.specimenLabelFmt}),
        form: {},
        open: true
      });
    }

    $scope.remove = function(event, index) {
      event.stopPropagation();

      Util.showConfirm({
        title: 'specimens.delete_q',
        confirmMsg: 'specimens.confirm_q',
        isWarning: true,
        ok: function() {
          inputCtxts.splice(index, 1);
          if (inputCtxts.length == 0) {
            $scope.addAnother();
          }
        }
      });
    }

    $scope.addCopyOfLast = function(reqAliquots) {
      var lastCtrl = inputCtxts[inputCtxts.length - 1].form.ctrl;

      var lastSpmn = lastCtrl.getSpecimens()[0];
      if (!!lastSpmn) {
        var spmn = angular.copy(lastSpmn);
        delete spmn.$$count;

        var aliquots = reqAliquots ? angular.copy(lastCtrl.getAliquotSpec()) : undefined;
        inputCtxts.push({
          specimen: spmn,
          aliquots: aliquots,
          form: {},
          open: true
        });
      }
    }

    $scope.next = function() {
      var error = false, specimensToCollect = [];
      angular.forEach(inputCtxts,
        function(ctxt) {
          var spmns = ctxt.form.ctrl.getSpecimens(true);

          if (spmns) {
            spmns[0].selected = true;
            spmns[0].status = 'Pending';

            var count = spmns[0].$$count;
            count = count || 1;
            for (var i = 0; i < +count; ++i) {
              if (i != 0) {
                spmns = angular.copy(spmns);

                var location = spmns[0].storageLocation;
                if (location) {
                  spmns[0].storageLocation = { name: location.name, mode: location.mode };
                }

                delete spmns[0].$$count;
              }

              Array.prototype.push.apply(specimensToCollect, spmns);
            }
          } else {
            error = true;
          }
        }
      );

      if (error) {
        return;
      }

      var opts = {showCollVisitDetails: false};
      CollectSpecimensSvc.collect(getState(), visit, specimensToCollect, opts);
    }

    $scope.update = function() {
      var input = inputCtxts[0].form.ctrl.getSpecimens();
      if (!input || input.length == 0) {
        return;
      }

      input[0].$saveOrUpdate().then(
        function(result) {
          angular.extend(specimen, result);
          ParticipantSpecimensViewState.specimensUpdated($scope);

          var params = {specimenId: result.id, cprId: result.cprId, visitId: result.visitId, srId: result.reqId};
          $state.go('specimen-detail.overview', params);
        }
      );
    }

    init();
  })
  .directive('osSpecimenAddeditForm', function($rootScope, Specimen, SpecimenUtil, Util, PvManager, ExtensionsUtil) {

    function loadPvs(scope) {
      scope.biohazards       = PvManager.getPvs('specimen-biohazard');
      scope.specimenStatuses = PvManager.getPvs('specimen-status');
    };

    return {
      restrict: 'E',

      templateUrl: 'modules/biospecimen/participant/specimen/addedit-form.html',

      scope: {
        opts    : '=',
        specimen: '=',
        aliquots: '=',
        ctrl    : '='
      },

      controller: function($scope) {
        this.getSpecimens = function(reqAliquots) {
          var formCtrl = $scope.deFormCtrl.ctrl;
          if (formCtrl && !formCtrl.validate()) {
            return null;
          }

          var primarySpmn = $scope.inputSpmn;
          if (formCtrl) {
            primarySpmn.extensionDetail = formCtrl.getFormData();
          }

          var result = [primarySpmn];

          var spmnCtx = $scope.spmnCtx, children = [];
          if (reqAliquots && spmnCtx.createAliquots) {
            angular.forEach(spmnCtx.aliquots,
              function(aliquotSpec) {
                var detail = angular.copy({aliquotSpec: aliquotSpec});
                angular.extend(detail, {parentSpecimen: primarySpmn, deFormCtrl: {}, cpr: $scope.opts.cpr});

                var tree = SpecimenUtil.collectAliquots(detail);
                tree.splice(0, 1); // remove parent as it is already in our final list
                Array.prototype.push.apply(result, tree);

                Array.prototype.push.apply(children, primarySpmn.children); // accumulate direct children of primary spmns
              }
            );
          }

          primarySpmn.children = children;
          return result;
        }

        this.getAliquotSpec = function() {
          return ($scope.spmnCtx.createAliquots && $scope.spmnCtx.aliquots) || [];
        }
      },

      link: function(scope, element, attrs, ctrl) {
        var opts = scope.opts, specimen = scope.specimen;
        scope.ctrl.ctrl = ctrl;

        var inputSpmn = scope.inputSpmn = specimen; //angular.copy(specimen);
        delete inputSpmn.children;

        inputSpmn.cpId = inputSpmn.cpId || opts.cp.id;
        inputSpmn.visitId = opts.visit && opts.visit.id;
        inputSpmn.createdOn = inputSpmn.createdOn || new Date();

        if (inputSpmn.lineage == 'Aliquot') {
          inputSpmn.anatomicSite = inputSpmn.laterality = undefined;
        }

        if (inputSpmn.status != 'Collected') {
          if (!inputSpmn.id) {
            inputSpmn.status = 'Collected';
          }

          inputSpmn.availableQty = inputSpmn.initialQty;
        }

        if (!inputSpmn.labelFmt) {
          if (specimen.lineage == 'New') {
            inputSpmn.labelFmt = opts.cpr.specimenLabelFmt;
          } else if (specimen.lineage == 'Aliquot') {
            inputSpmn.labelFmt = opts.cpr.aliquotLabelFmt;
          } else if (specimen.lineage == 'Derived') {
            inputSpmn.labelFmt = opts.cpr.derivativeLabelFmt;
          }
        }

        var exObjs = ['specimen.lineage', 'specimen.parentLabel', 'specimen.events'];
        if (!inputSpmn.id && !inputSpmn.reqId) {
          var currentDate = new Date();
          inputSpmn.collectionEvent = {
            user: $rootScope.currentUser,
            time: currentDate
          };

          inputSpmn.receivedEvent = {
            user: $rootScope.currentUser,
            time: currentDate
          };

          inputSpmn.collectionEvent.container = "Not Specified";
          inputSpmn.collectionEvent.procedure = "Not Specified";
          inputSpmn.receivedEvent.receivedQuality = "Acceptable";
        }

        if (inputSpmn.lineage != 'New') {
          exObjs.push('specimen.collectionEvent', 'specimen.receivedEvent');
        }

        inputSpmn.initialQty = Util.getNumberInScientificNotation(inputSpmn.initialQty);
        inputSpmn.availableQty = Util.getNumberInScientificNotation(inputSpmn.availableQty);
        inputSpmn.concentration = Util.getNumberInScientificNotation(inputSpmn.concentration);

        var spmnCtx = scope.spmnCtx = {
          obj: {specimen: inputSpmn, cpr: opts.cpr, visit: opts.visit, cp: opts.cp},
          inObjs: ['specimen'], exObjs: exObjs,
          isVirtual: inputSpmn.showVirtual(),
          manualSpecLabelReq: !!inputSpmn.label || !inputSpmn.labelFmt || opts.cp.manualSpecLabelEnabled,
          aliquots: scope.aliquots || [new Specimen({lineage: 'Aliquot'})],
          createAliquots: (scope.aliquots || []).length > 0
        };

        scope.deFormCtrl = {};
        scope.extnOpts = ExtensionsUtil.getExtnOpts(inputSpmn, opts.extensionCtxt);

        if (!opts.hasDict) {
          loadPvs(scope);
        }

        scope.toggleAliquots = function() {
          spmnCtx.aliquots = [new Specimen({lineage: 'Aliquot'})];
        }

        scope.addAnotherAliquot = function() {
          spmnCtx.aliquots.push(new Specimen({lineage: 'Aliquot'}));
        }

        scope.removeAliquot = function(idx) {
          spmnCtx.aliquots.splice(idx, 1);
          if (spmnCtx.aliquots.length == 0) {
            spmnCtx.aliquots.push(new Specimen({lineage: 'Aliquot'}));
          }
        }
      }
    }
  });
