
angular.module('os.administrative.shipment.addedit', ['os.administrative.models', 'os.biospecimen.models'])
  .controller('ShipmentAddEditCtrl', function(
    $scope, $state, shipment, shipmentItems, Shipment,
    Site, Specimen, SpecimensHolder, Alerts, Util, SpecimenUtil) {

    function init() {
      var spmnShipment = $scope.spmnShipment = shipment.isSpecimenShipment();
      shipment[spmnShipment ? 'shipmentSpmns' : 'shipmentContainers'] = shipmentItems || [];

      $scope.shipment = shipment;
      $scope.spmnOpts = {filters: {}, error: {}};
      $scope.input = {};

      if (!shipment.id && angular.isArray(SpecimensHolder.getSpecimens())) {
        shipment.shipmentSpmns = getShipmentSpecimens(SpecimensHolder.getSpecimens());
        SpecimensHolder.setSpecimens(null);
      }

      if (!shipment.id && spmnShipment && !areSpmnsOfSameSite(shipment.shipmentSpmns)) {
        Alerts.error('shipments.multi_site_specimens');
        $scope.back();
        return;
      }

      if (!shipment.shippedDate) {
        shipment.shippedDate = new Date();
      }

      setUserAndSiteList(shipment);
    }

    function areSpmnsOfSameSite(shipmentSpmns) {
      if (!shipmentSpmns) {
        return true;
      }

      var site, sameSite = true;
      for (var i = 0; i < shipmentSpmns.length; ++i) {
        var spmn = shipmentSpmns[i].specimen;

        if (!spmn.storageSite) {
          continue;
        }

        if (!!site && site != spmn.storageSite) {
          sameSite = false;
          break;
        }

        site = spmn.storageSite;
      }

      if (sameSite) {
        $scope.shipment.sendingSite = site;
      }

      return sameSite;
    }
    
    function loadRecvSites(instituteName, searchTerm) {
      return Site.listForInstitute(instituteName, true, searchTerm).then(
        function(sites) {
          return sites;
        }
      );
    }

    function loadSendingSites(searchTerm) {
      return Site.list({name: searchTerm});
    }

    function setUserFilterOpts(institute) {
      $scope.userFilterOpts = {institute: institute};
    }

    function setUserAndSiteList(shipment) {
      var instituteName = shipment.receivingInstitute;
      if (instituteName) {
        setUserFilterOpts(instituteName);
      }
    }

    function getShipmentSpecimens(specimens) {
      return specimens.filter(
        function(specimen) {
          return (specimen.availableQty == undefined || specimen.availableQty > 0)
                 && specimen.activityStatus == 'Active';
        }
      ).map(
        function(specimen) {
          return {
            specimen: specimen
          };
        }
      );
    }

    function getShipmentContainers(containers) {
      return containers.map(
        function(container) {
          return {
            container: container,
            specimensCount: container.storedSpecimens
          } 
        }
      );
    }

    function saveOrUpdate(status) {
      var shipmentClone = angular.copy($scope.shipment);
      shipmentClone.status = status;
      shipmentClone.$saveOrUpdate().then(
        function(savedShipment) {
          $state.go('shipment-detail.overview', {shipmentId: savedShipment.id});
        }
      );
    };

    function getValidationMsgKeys(useBarcode) {
      return {
        title:         'shipments.specimen_validation.title',
        foundCount:    'shipments.specimen_validation.found_count',
        notFoundCount: 'shipments.specimen_validation.not_found_count',
        notFoundError: 'shipments.specimen_validation.not_found_error',
        extraCount:    'shipments.specimen_validation.extra_count',
        extraError:    'shipments.specimen_validation.extra_error',
        itemLabel:     useBarcode ? 'specimens.barcode' : 'specimens.label',
        error:         'common.error'
      }
    }

    $scope.loadSendingSites = loadSendingSites;

    $scope.loadRecvSites = loadRecvSites;

    $scope.onInstituteSelect = function(instituteName) {
      $scope.shipment.receivingSite = undefined;
      $scope.shipment.notifyUsers = [];

      setUserFilterOpts(instituteName);
    }

    $scope.onSiteSelect = function(siteName) {
      Site.getByName(siteName).then(
        function(site) {
          $scope.shipment.notifyUsers = site.coordinators;
        }
      );
    }

    $scope.initSpmnOpts = function(forward) {
      if (forward) {
        $scope.spmnOpts = {
          filters: {
            storageLocationSite: $scope.shipment.sendingSite
          },
          error: {
            code: 'specimens.specimen_not_found_at_send_site',
            params: {sendingSite: $scope.shipment.sendingSite}
          }
        }
      }

      return true;
    }

    $scope.addSpecimens = function(specimens) {
      if (!specimens) {
        return false;
      }

      Util.addIfAbsent($scope.shipment.shipmentSpmns, getShipmentSpecimens(specimens), 'specimen.id');
      return true;
    }

    $scope.removeShipmentItem = function(shipmentItem) {
      var collection = shipment[(shipment.type == 'SPECIMEN') ? 'shipmentSpmns' : 'shipmentContainers'];
      var idx = collection.indexOf(shipmentItem);
      if (idx != -1) {
        collection.splice(idx, 1);
      }
    }

    $scope.addContainers = function(names) {
      if (!names) {
        return false;
      }

      return shipment.searchContainers(names).then(
        function(containers) {
          Util.addIfAbsent(shipment.shipmentContainers, getShipmentContainers(containers), 'container.id');
          return true;
        }
      );
    }

    $scope.ship = function() {
      saveOrUpdate('Shipped');
    }

    $scope.saveDraft = function() {
      saveOrUpdate('Pending');
    }

    $scope.passThrough = function() {
      return true;
    }

    $scope.validateSpecimens = function(ctrl) {
      var prop = ctrl.useBarcode() ? 'specimen.barcode' : 'specimen.label';
      var result = Util.validateItems($scope.shipment.shipmentItems, ctrl.getLabels(), prop);
      Util.showItemsValidationResult(getValidationMsgKeys(ctrl.useBarcode()), result);
    }

    init();
  });
