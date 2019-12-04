package sonar;

import io.vavr.control.Try;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.io.FileUtils;
import sonar.dao.SonarDAO;
import sonar.entities.Statistics;
import sonar.utils.$;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.util.*;

@Log4j2
public class Main {
    public static void main(String[] args) throws FileAlreadyExistsException {
        //sonarqube服务地址
        String baseUrl = "127.0.0.1:9000";
        String fileName = "index.html";

        if (args.length == 1) {
            val arg0 = args[0];
            switch (arg0) {
                case "-h":
                case "--help":
                case "-help":
                case "--h":
                    printHelp();
                    System.exit(0);
                    break;
                default:
                    baseUrl = arg0;
            }
        }
        if (args.length == 2) {
            baseUrl = args[0];
            fileName = args[1];
        }

        val file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }

        log.info("正在从系统上读取数据....");
        val stats = generateStats(baseUrl);
        log.info("数据读取完毕，开始解析数据...");
        val sheet1 = transformTable1(stats);
        val sheet2 = transformTable2(stats);
        log.info("数据解析完毕，开始生成报表...");
        val template = $.readFileAsStringFromClassPath("/index.html").get();
        final String format = $.stringFormat(template, "{}", sheet1, sheet2);

        try (val fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            fw.write(format);
            fw.flush();
            log.info("报表生成好了！");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        System.exit(0);
    }

    private static void printHelp() {
        log.info("第一个参数（必须）：输入sonar的地址，如http://10.16.128.39:9000，注意以【http://】开头，结尾不能添加【/】");
        log.info("第二个参数（可选）：输入要生成的报告文件名称");
    }

    private static List<Statistics> generateStats(String baseUrl) {
        try (val dao = new SonarDAO(baseUrl)) {
            dao.login();
            val projects = dao.listProjects();
            val stats = new ArrayList<Statistics>();
            for (val project : projects) {
                val stat = new Statistics();
                val authorsOfProject = dao.listAuthorsOfProject(project.getKey()).entrySet();
                stat.setProjectName(project.getName());
                for (val author : authorsOfProject) {
                    val user = author.getKey();
                    stat.getUsers().add(user);
                    stat.getBugs().put(user, dao.listBugOfAuthor(project.getKey(), user));
                    stat.getVulnerabilities().put(user, dao.listVulnerabilityOfAuthor(project.getKey(), user));
                    stat.getBadsmells().put(user, dao.listCodeSmellOfAuthor(project.getKey(), user));
                }
                stats.add(stat);
            }
            return stats;
        }
    }

    private static String otd = "<td>";
    private static String ctd = "</td>";
    private static String oth = "<th>";
    private static String cth = "</th>";
    private static String otr = "<tr>";
    private static String ctr = "</tr>";

    private static String[] severities = {"BLOCKER", "CRITICAL", "MAJOR", "MINOR", "INFO"};
    // 为git提交用户姓名初始化赋值
    private static HashMap<String, String> mapUserName = new HashMap<String, String>() {
        {
            put("liyue", "李月");
            put("yangyang", "杨阳");
        }
    };

    private static String transformTable1(List<Statistics> stats) {
        val ret = new StringBuilder();
        for (int i = 0; i < stats.size(); i++) {
            val stat = stats.get(i);
            for (int j = 0; j < stat.getUsers().size(); j++) {
                val user = stat.getUsers().get(j);
                String userRep="";
                //遍历获取userName
                for (Map.Entry<String, String> entry : mapUserName.entrySet()) {
                    if(entry.getKey().equals(user)){
                        userRep=entry.getValue();
                    }
                }
                val htmlTr = new StringBuilder(otr);
                if (j == 0)
                    htmlTr.append("<td rowspan='").append(Math.max(1, stat.getUsers().size())).append("' style='font-size:1em;font-weight: bold'>").append(stat.getProjectName()).append(ctd);
                htmlTr.append(otd).append(userRep).append(ctd);
                int totalBugs = 0;
                for (val key : severities) {
                    val bugs = stat.getBugs().get(user).get(key);
                    htmlTr.append(generateDangerTd(bugs));
                    totalBugs += bugs;
                }
                int totalVulnerabilities = 0;
                for (val key : severities) {
                    val vulnerabilities = stat.getVulnerabilities().get(user).get(key);
                    htmlTr.append(generateDangerTd(vulnerabilities));
                    totalVulnerabilities += vulnerabilities;
                }
                int totalBadsmells = 0;
                for (val key : severities) {
                    val badsmells = stat.getBadsmells().get(user).get(key);
                    htmlTr.append(generateWarningTd(badsmells));
                    totalBadsmells += badsmells;
                }
                htmlTr.append(generateDangerTd(totalBugs));
                htmlTr.append(generateDangerTd(totalVulnerabilities));
                htmlTr.append(generateWarningTd(totalBadsmells));
                ret.append(htmlTr);
            }
        }
        return ret.toString();
    }

    private static String transformTable2(List<Statistics> stats) {
        val th = new StringBuilder("<tr><th rowspan='2'>作者</th>");
        for (val stat : stats) {
            th.append("<th colspan='3'>").append(stat.getProjectName()).append(cth);
        }
        th.append("<th colspan='4'>").append("统计").append(cth).append(ctr);
        th.append(otr);
        for (int i = 0; i <= stats.size(); i++) {
            th.append(oth).append("Bug").append(cth);
            th.append(oth).append("漏洞").append(cth);
            th.append(oth).append("异味").append(cth);
        }
        th.append(oth).append("总计").append(cth).append(ctr);
        val allUsers = new HashSet<String>();
        for (val stat : stats) {
            allUsers.addAll(stat.getUsers());
        }
        for (val user : allUsers) {
            String userRep="";
            //遍历获取userName
            for (Map.Entry<String, String> entry : mapUserName.entrySet()) {
                if(entry.getKey().equals(user)){
                    userRep=entry.getValue();
                }
            }
            val tr = new StringBuilder(otr).append(otd).append(userRep).append(ctd);

            int cntTotalBugs = 0;
            int cntTotalVulnerabilities = 0;
            int cntTotalBadsmells = 0;
            for (val stat : stats) {
                val totalBugs = Try.of(() -> stat.getBugs().get(user).values().stream().reduce(0, (a, b) -> a + b)).getOrElse(0);
                cntTotalBugs += totalBugs;
                tr.append(generateDangerTd(totalBugs));
                val totalVulnerabilities = Try.of(() -> stat.getVulnerabilities().get(user).values().stream().reduce(0, (a, b) -> a + b)).getOrElse(0);
                cntTotalVulnerabilities += totalVulnerabilities;
                tr.append(generateDangerTd(totalVulnerabilities));
                val totalBadsmells = Try.of(() -> stat.getBadsmells().get(user).values().stream().reduce(0, (a, b) -> a + b)).getOrElse(0);
                cntTotalBadsmells += totalBadsmells;
                tr.append(generateWarningTd(totalBadsmells));
            }
            tr.append(generateDangerTd(cntTotalBugs));
            tr.append(generateDangerTd(cntTotalVulnerabilities));
            tr.append(generateWarningTd(cntTotalBadsmells));
            tr.append(otd).append(cntTotalBugs + cntTotalVulnerabilities + cntTotalBadsmells).append(ctd);
            tr.append(ctr);
            th.append(tr);
        }
        return th.toString();
    }

    private static String generateDangerTd(int problemCnt) {
        if (problemCnt > 0)
            return "<td class='danger'>" + problemCnt + ctd;
        else
            return otd + problemCnt + ctd;
    }

    private static String generateWarningTd(int problemCnt) {
        if (problemCnt > 0)
            return "<td class='warning'>" + problemCnt + ctd;
        else
            return otd + problemCnt + ctd;
    }
}
