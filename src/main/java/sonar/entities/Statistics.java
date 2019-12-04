package sonar.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 这是一个统计类，用来收集从Sonar上读取的数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Statistics {
    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 该项目下的所有用户的名称
     */
    private List<String> users = new ArrayList<>();
    private Map<String, Map<String, Integer>> bugs = new HashMap<>();
    private Map<String, Map<String, Integer>> vulnerabilities = new HashMap<>();
    private Map<String, Map<String, Integer>> badsmells = new HashMap<>();
}
