angular.module('os.biospecimen.models.specimen', ['os.common.models'])
  .factory('Specimen', function(osModel, $http) {
    var Specimen = osModel(
      'specimens',
      function(specimen) {
        if (specimen.children) {
          specimen.children = specimen.children.map(
            function(child) {
              return new Specimen(child);
            }
          );
        }
      }
    );
 
    Specimen.listFor = function(cprId, visitDetail) {
      return Specimen.query(angular.extend({cprId: cprId}, visitDetail || {}));
    };

    Specimen.flatten = function(specimens, parent, depth) {
      var result = [];
      if (!specimens) {
        return result;
      }

      depth = depth || 0;
      for (var i = 0; i < specimens.length; ++i) {
        result.push(specimens[i]);
        specimens[i].depth = depth || 0;
        specimens[i].parent = parent;
        specimens[i].hasChildren = (!!specimens[i].children && specimens[i].children.length > 0);
        if (specimens[i].hasChildren) {
          result = result.concat(Specimen.flatten(specimens[i].children, specimens[i], depth +1));
        }
      }

      return result;
    };

    Specimen.save = function(specimens) {
      return $http.post(Specimen.url(), specimens).then(
        function(result) {
          return result.data;
        }
      );
    };

    Specimen.isUniqueLabel = function(label) {
      return $http.head(Specimen.url(), {params: {label: label}}).then(
        function(result) {
          return false;
        },

        function(result) {
          return true;
        }
      );
    };

    Specimen.prototype.hasSufficientQty = function() {
      var qty = this.initialQty;
      angular.forEach(this.children, function(child) {
        if (child.lineage == 'Aliquot') {
          qty -= child.initialQty;
        }
      });

      return qty >= 0;
    }

    Specimen.prototype.rootSpecimen = function() {
      var curr = this;
      while (curr.parent) {
        curr = curr.parent;
      }

      return curr;
    };

    return Specimen;
  });