package sonar.respond_entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sonar.entities.Project;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectsRespond {
    private List<Project> components;
}
