
angular.module('os.biospecimen.participant.specimen-tree', 
  [
    'os.biospecimen.models', 
    'os.biospecimen.participant.collect-specimens',
  ])
  .directive('osSpecimenTree', function(
    $state, $stateParams, $modal, $timeout,
    CollectSpecimensSvc, Specimen, SpecimenLabelPrinter, SpecimensHolder,
    Alerts, PvManager, Util, DeleteUtil, SpecimenUtil) {

    function openSpecimenTree(specimens) {
      angular.forEach(specimens, function(specimen) {
        specimen.isOpened = true;
        openSpecimenTree(specimen.children);
      });
    }

    function toggleAllSelected(selection, specimens, specimen) {
      if (!specimen.selected) {
        selection.all = false;
        return;
      }

      for (var i = 0; i < specimens.length; ++i) {
        if (!specimens[i].selected) {
          selection.all = false;
          return;
        }
      }

      selection.all = true;
    };

    function selectParentSpecimen(specimen) {
      if (!specimen.selected) {
        return false;
      }

      var parent = specimen.parent;
      while (parent) {
        parent.selected = true;
        parent = parent.parent;
      }
    };

    function isAnySelected(specimens) {
      for (var i = 0; i < specimens.length; ++i) {
        if (specimens[i].selected) {
          return true;
        }
      }

      return false;
    }

    function isAnyChildOrPooledSpecimenSelected(specimen) {
      if (!!specimen.pooledSpmns) {
        for (var i = 0; i < specimen.pooledSpmns.length; ++i) {
          if (specimen.pooledSpmns[i].selected) {
            return true;
          }
        }
      }

      if (!specimen.children) {
        return false;
      }

      for (var i = 0; i < specimen.children.length; ++i) {
        if (specimen.children[i].selected) {
          return true;
        }

        if (isAnyChildOrPooledSpecimenSelected(specimen.children[i])) {
          return true;
        }
      }

      return false;
    };

    function getState() {
      return {state: $state.current, params: $stateParams};
    };

    function showSelectSpecimens(msgCode) {
      Alerts.error(msgCode);
    };

    function getSelectedSpecimens (scope, message, anyStatus) {
      if (!scope.selection.any) {
        showSelectSpecimens(message);
        return;
      }

      var specimens = [];
      angular.forEach(scope.specimens, function(specimen) {
        if (!specimen.selected) {
          return;
        }

        if ((specimen.status == 'Collected' || anyStatus) && specimen.id) {
          specimens.push(specimen);
        }
      });

      if (specimens.length == 0) {
        showSelectSpecimens(message);
        return;
      }

      return specimens;
    };

    return {
      restrict: 'E',

      scope: {
        cp: '=',
        cpr: '=',
        visit: '=',
        specimenTree: '=specimens',
        allowedOps: '=',
        reload: '&reload'
      },

      templateUrl: 'modules/biospecimen/participant/specimens.html',

      link: function(scope, element, attrs) {
        scope.view = 'list';
        scope.parentSpecimen = undefined;

        scope.specimens = Specimen.flatten(scope.specimenTree);
        openSpecimenTree(scope.specimens);

        scope.openSpecimenNode = function(specimen) {
          specimen.isOpened = true;
        };

        scope.closeSpecimenNode = function(specimen) {
          specimen.isOpened = false;
        };

        scope.selection = {all: false, any: false};
        scope.toggleAllSpecimenSelect = function() {
          angular.forEach(scope.specimens, function(specimen) {
            specimen.selected = scope.selection.all;
          });

          scope.selection.any = scope.selection.all;
        };

        scope.toggleSpecimenSelect = function(specimen) {
          if (specimen.status != 'Collected') {
            selectParentSpecimen(specimen);
          }

          toggleAllSelected(scope.selection, scope.specimens, specimen);

          scope.selection.any = specimen.selected ? true : isAnySelected(scope.specimens);
        };

        scope.collectSpecimens = function() {
          if (!scope.selection.any) {
            showSelectSpecimens('specimens.no_specimens_for_collection');
            return;
          }

          var specimensToCollect = [];
          angular.forEach(scope.specimens, function(specimen) {
            if (specimen.selected) {
              specimen.isOpened = true;
              specimensToCollect.push(specimen);
            } else if (isAnyChildOrPooledSpecimenSelected(specimen)) {
              if (specimen.status != 'Collected') {
                // a parent needs to be collected first
                specimen.selected = true;
              }
              specimen.isOpened = true;
              specimensToCollect.push(specimen);
            }
          });

          var onlyCollected = true;
          for (var i = 0; i < specimensToCollect.length; ++i) {
            if (specimensToCollect[i].status != 'Collected') {
              onlyCollected = false;
              break;
            }
          }

          if (onlyCollected) {
            showSelectSpecimens('specimens.no_specimens_for_collection');
            return;
          }

          CollectSpecimensSvc.collect(getState(), scope.visit, specimensToCollect);
        };

        scope.printSpecimenLabels = function() {
          var specimensToPrint = getSelectedSpecimens(scope, 'specimens.no_specimens_for_print', false);
          if (specimensToPrint == undefined || specimensToPrint.length == 0) {
            return;
          }

          var specimenIds = getSpecimenIds(specimensToPrint);
          SpecimenLabelPrinter.printLabels({specimenIds: specimenIds});
        };

        scope.deleteSpecimens = function() {
          var specimensToDelete = getSelectedSpecimens(scope, 'specimens.no_specimens_for_delete', true);
          if (specimensToDelete.length == 0) {
            return;
          }

          var specimenIds = getSpecimenIds(specimensToDelete);
          DeleteUtil.bulkDelete(Specimen, specimenIds, getBulkDeleteOpts(specimensToDelete));
          scope.selection.all = false;
        }

        scope.closeSpecimens = function() {
          var specimensToClose = getSelectedSpecimens(scope, 'specimens.no_specimens_for_close', false);
          if (specimensToClose.length == 0) {
            return;
          }

          var modalInstance = $modal.open({
            templateUrl: 'modules/biospecimen/participant/specimen/close.html',
            controller: 'SpecimenCloseCtrl',
            resolve: {
              specimens: function() {
                return specimensToClose;
              }
            }
          });
          scope.selection.all = false;
        };

        scope.addSpecimensToSpecimenList = function(list) {
          if (!scope.selection.any) {
            showSelectSpecimens('specimens.no_specimens_for_specimen_list');
            return;
          }
          var selectedSpecimens = [];
          getSelectedSpecimens(scope, null, true).map(
            function(specimen) {
              selectedSpecimens.push({label: specimen.label});
            }
          );

          if (!!list) {
            list.addSpecimens(selectedSpecimens).then(function(specimens) {
              Alerts.success('specimen_list.specimens_added', {name: list.name});
            })
          } else {
            SpecimensHolder.setSpecimens(selectedSpecimens);
            $state.go('specimen-list-addedit', {listId: ''});
          }
        }

        scope.showCreateAliquots = function(specimen) {
          scope.view = 'create_aliquots';      
          scope.parentSpecimen = specimen;
          scope.aliquotSpec = {createdOn : new Date()};
        };

        scope.collectAliquots = function() {
          var specimens = SpecimenUtil.collectAliquots(scope);
          CollectSpecimensSvc.collect(getState(), scope.visit, specimens, parent);
        };

        scope.loadSpecimenTypes = function(specimenClass, notClear) {
          SpecimenUtil.loadSpecimenTypes(scope, specimenClass, notClear);
        };

        scope.showCreateDerivative = function(specimen) {
          scope.view = 'create_derivatives';      
          scope.parentSpecimen = specimen;
          scope.derivative = SpecimenUtil.getNewDerivative(scope);
          SpecimenUtil.loadSpecimenClasses(scope);
        };

        scope.createDerivative = function() {
          SpecimenUtil.createDerivatives(scope);
        };

        scope.showCloseSpecimen = function(specimen) {
          scope.view = 'close_specimen';
          scope.specStatus = { reason: '' };
          scope.parentSpecimen = specimen;
        };

        scope.closeSpecimen = function() {
          scope.parentSpecimen.close(scope.specStatus.reason).then(
            function() {
              scope.revertEdit();
            }
          );
        };
         
        scope.revertEdit = function() {
          scope.view = 'list';
          scope.parentSpecimen = undefined;
        }

        function getSpecimenIds(specimens) {
          return specimens.map(
            function(s) {
              return s.id;
            }
          );
        }

        function getBulkDeleteOpts(specimensToDelete) {
          var hasChildren = childrenExists(specimensToDelete);
          return {
            confirmDelete : hasChildren ? 'specimens.delete_specimens_heirarchy' :'specimens.delete_specimens',
            successMessage: hasChildren ? 'specimens.specimens_hierarchy_deleted' : 'specimens.specimens_deleted',
            onBulkDeletion: function(result) {
              if (typeof scope.reload == "function") {
                scope.reload().then(
                  function() {
                    $timeout(function() {
                      scope.specimens = Specimen.flatten(scope.specimenTree);
                      openSpecimenTree(scope.specimens);
                    });
                  }
                );
              }
              scope.selection.any = false;
            }
          }
        }

        function childrenExists(specimens) {
          for (var i = 0; i < specimens.length; ++i) {
            if (specimens[i].children.length > 0) {
              return true;
            }
          }
          return false;
        }
      }
    }
  });
