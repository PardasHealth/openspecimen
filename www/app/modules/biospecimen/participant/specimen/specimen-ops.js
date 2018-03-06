angular.module('os.biospecimen.specimen')
  .directive('osSpecimenOps', function(
    $state, $rootScope, $modal, $q, DistributionProtocol, DistributionOrder,
    Specimen, SpecimensHolder, Alerts, CommentsUtil, DeleteUtil, ParticipantSpecimensViewState) {

    function initOpts(scope) {
      if (!scope.resourceOpts) {
        var cpShortTitle = scope.cp && scope.cp.shortTitle;
        var sites = undefined;
        if (scope.cp) {
          sites = scope.cp.cpSites.map(function(cpSite) { return cpSite.siteName; });
          if ($rootScope.global.appProps.mrn_restriction_enabled && scope.cpr) {
            sites = sites.concat(scope.cpr.getMrnSites());
          }
        }

        scope.resourceOpts = {
          orderCreateOpts:    {resource: 'Order', operations: ['Create']},
          shipmentCreateOpts: {resource: 'ShippingAndTracking', operations: ['Create']},
          specimenUpdateOpts: {cp: cpShortTitle, sites: sites, resource: 'VisitAndSpecimen', operations: ['Update']},
          specimenDeleteOpts: {cp: cpShortTitle, sites: sites, resource: 'VisitAndSpecimen', operations: ['Delete']}
        };
      }

      initAllowDistribution(scope);
    }

    function initAllowDistribution(scope) {
      if (!scope.cp) {
        scope.allowDistribution = true;
        return;
      }

      if (!!scope.cp.distributionProtocols && scope.cp.distributionProtocols.length > 0) {
        scope.allowDistribution = true;
      } else {
        DistributionProtocol.getCount({cp: scope.cp.shortTitle, excludeExpiredDps: true}).then(
          function(resp) {
            scope.allowDistribution = (resp.count > 0);
          }
        );
      }
    }

    function getDp(scope, hideDistributeBtn) {
      return $modal.open({
        templateUrl: 'modules/biospecimen/participant/specimen/distribute.html',
        controller: function($scope, $modalInstance) {
          var ctx;

          function init() {
            ctx = $scope.ctx = {
              defDps: undefined,
              dps: [],
              dp: undefined,
              hideDistributeBtn: hideDistributeBtn
            };
          }

          function loadDps(searchTerm) {
            var cpShortTitle;
            if (scope.cp) {
              if (scope.cp.distributionProtocols && scope.cp.distributionProtocols.length > 0) {
                ctx.dps = scope.cp.distributionProtocols;
                if (!ctx.defDps) {
                  ctx.defDps = ctx.dps;
                  if (ctx.dps.length == 1) {
                    ctx.dp = ctx.dps[0];
                  }
                }

                return;
              }

              cpShortTitle = scope.cp.shortTitle;
            }

            if (ctx.defDps && (!searchTerm || ctx.defDps.length <= 100)) {
              ctx.dps = ctx.defDps;
              return;
            }

            DistributionProtocol.query({query: searchTerm, cp: cpShortTitle, excludeExpiredDps: true}).then(
              function(dps) {
                if (!searchTerm && !ctx.defDps) {
                  ctx.defDps = dps;
                  if (dps.length == 1) {
                    ctx.dp = dps[0];
                  }
                }

                ctx.dps = dps;
              }
            );
          }

          $scope.cancel = function() {
            $modalInstance.dismiss('cancel');
          }

          $scope.distribute = function() {
            $scope.ctx.distribute = true;
            $modalInstance.close($scope.ctx);
          }

          $scope.reserve = function() {
            $modalInstance.close($scope.ctx);
          }

          $scope.loadDps = loadDps;

          init();
        },

        size: 'lg'
      }).result;
    }

    function selectDpAndDistributeSpmns(scope, specimens, hideDistributeBtn) {
      getDp(scope, hideDistributeBtn).then(
        function(details) {
          if (details.distribute) {
            distributeSpmns(scope, details, specimens);
          } else {
            reserveSpmns(scope, details, specimens);
          }
        }
      );
    }

    function distributeSpmns(scope, details, specimens) {
      var dp = details.dp;

      new DistributionOrder({
        name: dp.shortTitle + '_' + new Date().toLocaleString(),
        distributionProtocol: dp,
        requester: dp.principalInvestigator,
        siteName: dp.defReceivingSiteName,
        orderItems: getOrderItems(specimens),
        comments: details.comments,
        status: 'EXECUTED'
      }).$saveOrUpdate().then(
        function(createdOrder) {
          Alerts.success('orders.creation_success', createdOrder);
          ParticipantSpecimensViewState.specimensUpdated(scope, {inline: true});
          scope.initList();
        }
      );
    }

    function getOrderItems(specimens) {
      return specimens.map(
        function(specimen) {
          return {
            specimen: specimen,
            quantity: specimen.availableQty,
            status: 'DISTRIBUTED_AND_CLOSED'
          }
        }
      );
    }

    function reserveSpmns(scope, details, specimens) {
      var request = {
        dpId: details.dp.id,
        comments: details.comments,
        specimens: specimens.map(function(spmn) { return {id: spmn.id }; })
      };

      details.dp.reserveSpecimens(request).then(
        function(resp) {
          Alerts.success('orders.specimens_reserved', {count: resp.updated});
          specimens.forEach(function(spmn) { return spmn.reserved = true; });
        }
      );
    }

    return {
      restrict: 'E',

      replace: true,

      scope: {
        cp: '=?',
        cpr: '=?',
        specimens: '&',
        initList: '&',
        resourceOpts: '=?'
      },

      templateUrl: 'modules/biospecimen/participant/specimen/specimen-ops.html',

      link: function(scope, element, attrs) {
        initOpts(scope);

        function gotoView(state, params, msgCode, anyStatus) {
          var selectedSpmns = scope.specimens({anyStatus: anyStatus});
          if (!selectedSpmns || selectedSpmns.length == 0) {
            Alerts.error('specimen_list.' + msgCode);
            return;
          }

          var specimenIds = selectedSpmns.map(function(spmn) {return spmn.id});
          Specimen.getByIds(specimenIds).then(
            function(spmns) {
              SpecimensHolder.setSpecimens(spmns);
              $state.go(state, params);
            }
          );
        }

        scope.editSpecimens = function() {
          var spmns = scope.specimens({anyStatus: true});
          if (!spmns || spmns.length == 0) {
            Alerts.error('specimen_list.no_specimens_to_edit');
            return;
          }

          SpecimensHolder.setSpecimens(spmns);
          $state.go('specimen-bulk-edit');
        }

        scope.deleteSpecimens = function() {
          var spmns = scope.specimens({anyStatus: true});
          if (!spmns || spmns.length == 0) {
            Alerts.error('specimens.no_specimens_for_delete');
            return;
          }

          var specimenIds = spmns.map(function(spmn) { return spmn.id; });
          var opts = {
            confirmDelete: 'specimens.delete_specimens_heirarchy',
            successMessage: 'specimens.specimens_hierarchy_deleted',
            onBulkDeletion: function() {
              ParticipantSpecimensViewState.specimensUpdated(scope, {inline: true});
              scope.initList();
            }
          }
          DeleteUtil.bulkDelete({bulkDelete: Specimen.bulkDelete}, specimenIds, opts);
        }

        scope.closeSpecimens = function() {
          var specimensToClose = scope.specimens({anyStatus: false});
          if (specimensToClose.length == 0) {
            Alerts.error('specimens.no_specimens_for_close');
            return;
          }

          $modal.open({
            templateUrl: 'modules/biospecimen/participant/specimen/close.html',
            controller: 'SpecimenCloseCtrl',
            resolve: {
              specimens: function() {
                return specimensToClose;
              }
            }
          }).result.then(
            function() {
              ParticipantSpecimensViewState.specimensUpdated(scope, {inline: true});
              scope.initList();
            }
          );
        };

        scope.distributeSpecimens = function() {
          if (!scope.cp) {
            gotoView('order-addedit', {orderId: ''}, 'no_specimens_for_distribution', false);
          } else {
            var selectedSpmns = scope.specimens({anyStatus: false});
            if (!selectedSpmns || selectedSpmns.length == 0) {
              Alerts.error('specimen_list.no_specimens_for_distribution');
              return;
            }

            selectDpAndDistributeSpmns(scope, selectedSpmns);
          }
        }

        scope.reserveSpecimens = function() {
          var selectedSpmns = scope.specimens({anyStatus: false});
          if (!selectedSpmns || selectedSpmns.length == 0) {
            Alerts.error('specimen_list.no_specimens_for_reservation');
            return;
          }

          selectDpAndDistributeSpmns(scope, selectedSpmns, true);
        }

        scope.shipSpecimens = function() {
          gotoView('shipment-addedit', {shipmentId: ''}, 'no_specimens_for_shipment');
        }

        scope.createAliquots = function() {
          gotoView('specimen-bulk-create-aliquots', {}, 'no_specimens_to_create_aliquots');
        }

        scope.createDerivatives = function() {
          gotoView('specimen-bulk-create-derivatives', {}, 'no_specimens_to_create_derivatives');
        }

        scope.addEvent = function() {
          gotoView('bulk-add-event', {}, 'no_specimens_to_add_event');
        }

        scope.retrieveSpecimens = function() {
          var selectedSpmns = scope.specimens();
          if (!selectedSpmns || selectedSpmns.length == 0) {
            Alerts.error('specimen_list.no_specimens_to_retrieve');
            return;
          }

          var ctx = {
            header: 'specimen_list.retrieve_specimens', headerParams: {},
            placeholder: 'specimen_list.retrieve_reason',
            button: 'specimen_list.retrieve_specimens'
          };
          CommentsUtil.getComments(ctx,
            function(comments) {
              var spmnsToUpdate = selectedSpmns.map(
                function(spmn) {
                  return {id: spmn.id, storageLocation: {}, transferComments: comments};
                }
              );
              Specimen.bulkUpdate(spmnsToUpdate).then(
                function(updatedSpmns) {
                  ParticipantSpecimensViewState.specimensUpdated(scope, {inline: true});
                  scope.initList();
                }
              );
            }
          );
        }
      }
    };
  });
