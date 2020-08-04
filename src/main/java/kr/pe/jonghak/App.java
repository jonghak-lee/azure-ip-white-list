package kr.pe.jonghak;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Predicate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

public class App 
{
    public static void main( String[] args ) throws FileNotFoundException {
        String jsonpathChangeNumber = "$['changeNumber']";
        String jsonpathValues = "$.values[?]";

        DocumentContext jsonContext = JsonPath.parse(new FileInputStream(new File("/Users/jonghak/Downloads/ServiceTags_Public_20200727.json")));
        Integer changeNumber = jsonContext.read(jsonpathChangeNumber);
        System.out.println("Azure IP Ranges and Service Tags Change Number: " + changeNumber);

        Predicate isKoreaCentral = new Predicate() {
            @Override
            public boolean apply(PredicateContext predicateContext) {
                String name = String.valueOf(predicateContext.item(Map.class).get("name"));
                return name.contains("KoreaCentral") || name.contains("KoreaSouth");
            }
        };

        List<Map<String, Object>> valuesOfKorea = jsonContext.read(jsonpathValues, isKoreaCentral);
        valuesOfKorea.forEach(value -> {
            System.out.println("===================================================");
            System.out.println("name: " + value.get("name"));
            System.out.println("id: " + value.get("id"));
            Map<String, Object> properties = (Map<String, Object>) value.get("properties");
            System.out.println("changeNumber: " + properties.get("changeNumber"));
            System.out.println("systemService: " + properties.get("systemService"));
            List<String> addressPrefixes = (List<String>) properties.get("addressPrefixes");
            System.out.println("addressPrefixes: ");
            addressPrefixes.forEach(addressPrefix -> System.out.println(addressPrefix));
            System.out.println("===================================================");
        });
    }
}
