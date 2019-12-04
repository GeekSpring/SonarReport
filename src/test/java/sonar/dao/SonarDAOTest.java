package sonar.dao;

import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Log4j2
class SonarDAOTest {

    private SonarDAO dao;

    @BeforeEach
    void setUp() {
         //soanrqueb服务地址
        dao = new SonarDAO("http://127.0.0.1:9000");
        dao.login();
    }

    @AfterEach
    void tearDown() {
        dao.close();
    }

    @Test
    void listProjects() {
        val projects = dao.listProjects();
        log.info("sonar项目名称："+projects);
    }

    @Test
    void listUsers() {
        val users = dao.listUsers();
        log.info("sonar用户："+users);
    }

    @Test
    void listAuthorsByKey() {
        log.info(dao.listAuthorsOfProject("com.caxs:gps-parent"));
    }

    @Test
    void listBugOfAuthor() {
        log.info(dao.listBugOfAuthor("com.caxs:gps-parent", "liyue"));
    }

    @Test
    void listVulnerabilityOfAuthor() {
        log.info(dao.listVulnerabilityOfAuthor("com.caxs:gps-parent", "liyue"));
    }

    @Test
    void listCodeSmellOfAuthor() {
        log.info(dao.listCodeSmellOfAuthor("com.caxs:gps-parent", "liyue"));
    }
}