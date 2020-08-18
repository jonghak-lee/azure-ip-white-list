package kr.pe.jonghak;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Predicate;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

public class App 
{
    public static void main( String[] args ) throws FileNotFoundException {
        if (args.length == 0) {
            System.out.println("Proper Usage is: Azure IP Ranges and Service Tags json file path and file name");
            // for examples:
            // "/Users/{user}/Downloads/ServiceTags_Public_{version}.json"
            // "~/Downloads/ServiceTags_Public_{version}.json"
            System.exit(0);
        }

        String pathname = "";

        if (args[0].startsWith("~")) {
            String userHome = System.getProperty("user.home");
            pathname = userHome + args[0].substring(1);
        } else {
            pathname = args[0];
        }

        String jsonpathChangeNumber = "$['changeNumber']";
        String jsonpathValues = "$.values[?]";

        DocumentContext jsonContext = JsonPath.parse(new FileInputStream(new File(pathname)));
        Integer changeNumber = jsonContext.read(jsonpathChangeNumber);
        System.out.println("Azure IP Ranges and Service Tags Change Number: " + changeNumber);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(String.valueOf(changeNumber));

        Predicate isKoreaCentral = new Predicate() {
            @Override
            public boolean apply(PredicateContext predicateContext) {
                String name = String.valueOf(predicateContext.item(Map.class).get("name"));
                return name.contains("KoreaCentral") || name.contains("KoreaSouth");
            }
        };

        Row header = sheet.createRow(0);

        header.createCell(0).setCellValue("name");
        header.createCell(1).setCellValue("id");
        header.createCell(2).setCellValue("changeNumber");
        header.createCell(3).setCellValue("systemService");
        header.createCell(4).setCellValue("addressPrefix");

        int rowCount = 1;

        List<Map<String, Object>> valuesOfKorea = jsonContext.read(jsonpathValues, isKoreaCentral);
        for (int i = 0; i < valuesOfKorea.size(); i++) {
            Map<String, Object> value = valuesOfKorea.get(i);
            Map<String, Object> properties = (Map<String, Object>) value.get("properties");
            List<String> addressPrefixes = (List<String>) properties.get("addressPrefixes");
            for (int j = 0; j < addressPrefixes.size(); j++) {
                String addressPrefix = addressPrefixes.get(j);

                Row row = sheet.createRow(rowCount);

                row.createCell(0).setCellValue(String.valueOf(value.get("name")));
                row.createCell(1).setCellValue(String.valueOf(value.get("id")));
                row.createCell(2).setCellValue(String.valueOf(properties.get("changeNumber")));
                row.createCell(3).setCellValue(String.valueOf(properties.get("systemService")));
                row.createCell(4).setCellValue(String.valueOf(addressPrefix));

                rowCount++;
            }

            try (FileOutputStream stream = new FileOutputStream("Azure_IP_Ranges_and_Service_Tags.xlsx")) {
                workbook.write(stream);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
