package sonar.respond_entities;

import io.vavr.Tuple2;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacetsRespond {

    private List<Facet> facets;

    public Map<String, Integer> toMapByProperty(String property) {
        val facet = getFacetByProperty(property);
        if (facet == null)
            return new HashMap<>();

        val ret = new HashMap<String, Integer>(facets.size());
        for (val value : facet.getValues()) {
            ret.put(value.getVal(), value.getCount());
        }
        return ret;
    }

    private Facet getFacetByProperty(String property) {
        for (Facet facet : facets) {
            if (facet.getProperty().equals(property))
                return facet;
        }
        return null;
    }
}
