package com.robiulsunyemon.profile_service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ModuleGenerator {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter base package (e.g., com.example.project): ");
        String basePackage = scanner.nextLine().trim();
        if (basePackage.isEmpty()) {
            System.out.println("Base package cannot be empty.");
            return;
        }

        System.out.print("Enter module name (e.g., Product): ");
        String moduleNameCapitalized = scanner.nextLine().trim();
        if (moduleNameCapitalized.isEmpty()) {
            System.out.println("Module name cannot be empty.");
            return;
        }

        String moduleName = moduleNameCapitalized.toLowerCase();

        System.out.print("Enter fields (e.g., name:String, price:Double) [Leave empty for no custom fields]: ");
        String fieldsInput = scanner.nextLine().trim();

        List<FieldDef> fields = new ArrayList<>();
        if (!fieldsInput.isEmpty()) {
            String[] splitFields = fieldsInput.split(",");
            for (String f : splitFields) {
                String[] parts = f.split(":");
                if (parts.length == 2) {
                    fields.add(new FieldDef(parts[0].trim(), parts[1].trim()));
                } else {
                    System.out.println("Invalid field format: " + f + ". Ignored.");
                }
            }
        }

        System.out.println("Generating module: " + moduleNameCapitalized + "...");

        try {
            String currentDir = System.getProperty("user.dir");
            createModule(currentDir, basePackage, moduleName, moduleNameCapitalized, fields);
            System.out.println("Module generation completed successfully in: " + currentDir + "\\" + moduleName);
        } catch (IOException e) {
            System.err.println("Error generating module: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createModule(String baseDir, String basePackage, String moduleName, String moduleNameCap, List<FieldDef> fields) throws IOException {
        String modulePath = baseDir + "\\" + moduleName;
        String modulePackage = basePackage + "." + moduleName;

        // Create directories
        createDir(modulePath + "\\controller");
        createDir(modulePath + "\\dto");
        createDir(modulePath + "\\entity");
        createDir(modulePath + "\\mapper");
        createDir(modulePath + "\\repository");
        createDir(modulePath + "\\service\\impl");

        // Generate files
        generateEntity(modulePath, modulePackage, moduleNameCap, fields);
        generateRequestDto(modulePath, modulePackage, moduleNameCap, fields);
        generateResponseDto(modulePath, modulePackage, moduleNameCap, fields);
        generateMapper(modulePath, modulePackage, moduleNameCap, fields);
        generateRepository(modulePath, modulePackage, moduleNameCap);
        generateService(modulePath, modulePackage, moduleNameCap);
        generateServiceImpl(modulePath, modulePackage, moduleNameCap);
        generateController(modulePath, modulePackage, moduleNameCap);
    }

    private static void createDir(String path) throws IOException {
        Path dirPath = Paths.get(path);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
    }

    private static void writeToFile(String path, String content) throws IOException {
        Files.writeString(Paths.get(path), content);
    }

    private static void generateEntity(String modulePath, String modulePackage, String moduleNameCap, List<FieldDef> fields) throws IOException {
        StringBuilder fieldsCode = new StringBuilder();
        for (FieldDef f : fields) {
            if (f.type.equals("String") && f.name.toLowerCase().contains("name")) {
                fieldsCode.append("    @Column(unique = true, nullable = false)\n");
            }
            fieldsCode.append("    private ").append(f.type).append(" ").append(f.name).append(";\n\n");
        }

        String content = "package " + modulePackage + ".entity;\n\n" +
                "import jakarta.persistence.*;\n" +
                "import lombok.Data;\n\n" +
                "import java.time.LocalDateTime;\n\n" +
                "@Entity\n" +
                "@Data\n" +
                "@Table(name = \"" + moduleNameCap.toLowerCase() + "s\")\n" +
                "public class " + moduleNameCap + "Entity {\n\n" +
                "    @Id\n" +
                "    @GeneratedValue(strategy = GenerationType.IDENTITY)\n" +
                "    private Long id;\n\n" +
                fieldsCode.toString() +
                "    private LocalDateTime createdAt;\n\n" +
                "    private LocalDateTime updatedAt;\n\n" +
                "    @PrePersist\n" +
                "    protected void setCreatedDate(){\n" +
                "        createdAt=LocalDateTime.now();\n" +
                "        updatedAt=LocalDateTime.now();\n" +
                "    }\n\n" +
                "    @PreUpdate\n" +
                "    protected void setUpdatedDate(){\n" +
                "        updatedAt=LocalDateTime.now();\n" +
                "    }\n" +
                "}\n";
        writeToFile(modulePath + "\\entity\\" + moduleNameCap + "Entity.java", content);
    }

    private static void generateRequestDto(String modulePath, String modulePackage, String moduleNameCap, List<FieldDef> fields) throws IOException {
        StringBuilder fieldsCode = new StringBuilder();
        boolean hasString = false;
        for (FieldDef f : fields) {
            if (f.type.equals("String") && f.name.toLowerCase().contains("name")) {
                fieldsCode.append("    @NotBlank\n");
            }
            fieldsCode.append("    private ").append(f.type).append(" ").append(f.name).append(";\n");
            if(f.type.equals("String")) hasString = true;
        }

        String content = "package " + modulePackage + ".dto;\n\n" +
                (hasString ? "import jakarta.validation.constraints.NotBlank;\n" : "") +
                "import lombok.Data;\n" +
                "import org.springframework.stereotype.Component;\n\n" +
                "@Component\n" +
                "@Data\n" +
                "public class " + moduleNameCap + "Request {\n" +
                fieldsCode.toString() +
                "}\n";
        writeToFile(modulePath + "\\dto\\" + moduleNameCap + "Request.java", content);
    }

    private static void generateResponseDto(String modulePath, String modulePackage, String moduleNameCap, List<FieldDef> fields) throws IOException {
        StringBuilder fieldsCode = new StringBuilder();
        for (FieldDef f : fields) {
            fieldsCode.append("    private ").append(f.type).append(" ").append(f.name).append(";\n");
        }

        String content = "package " + modulePackage + ".dto;\n" +
                "import lombok.Data;\n" +
                "import org.springframework.stereotype.Component;\n" +
                "import java.time.LocalDateTime;\n\n" +
                "@Component\n" +
                "@Data\n" +
                "public class " + moduleNameCap + "Response {\n" +
                "    private Long id;\n" +
                fieldsCode.toString() +
                "    private LocalDateTime createdAt;\n" +
                "    private LocalDateTime updatedAt;\n" +
                "}\n";
        writeToFile(modulePath + "\\dto\\" + moduleNameCap + "Response.java", content);
    }

    private static void generateMapper(String modulePath, String modulePackage, String moduleNameCap, List<FieldDef> fields) throws IOException {
        StringBuilder requestToEntity = new StringBuilder();
        StringBuilder entityToResponse = new StringBuilder();

        for (FieldDef f : fields) {
            String capitalizedName = f.name.substring(0, 1).toUpperCase() + f.name.substring(1);
            requestToEntity.append("        newEntity.set").append(capitalizedName).append("(request.get").append(capitalizedName).append("());\n");
            entityToResponse.append("        response.set").append(capitalizedName).append("(entity.get").append(capitalizedName).append("());\n");
        }

        String content = "package " + modulePackage + ".mapper;\n" +
                "import " + modulePackage + ".dto." + moduleNameCap + "Request;\n" +
                "import " + modulePackage + ".dto." + moduleNameCap + "Response;\n" +
                "import " + modulePackage + ".entity." + moduleNameCap + "Entity;\n" +
                "import org.springframework.stereotype.Component;\n\n" +
                "@Component\n" +
                "public class " + moduleNameCap + "Mapper {\n\n" +
                "    public " + moduleNameCap + "Entity requestToEntity (" + moduleNameCap + "Request request){\n" +
                "        " + moduleNameCap + "Entity newEntity = new " + moduleNameCap + "Entity();\n" +
                requestToEntity.toString() +
                "        return newEntity;\n" +
                "    }\n\n" +
                "    public " + moduleNameCap + "Response entityToResponse(" + moduleNameCap + "Entity entity){\n" +
                "        " + moduleNameCap + "Response response = new " + moduleNameCap + "Response();\n" +
                "        response.setId(entity.getId());\n" +
                entityToResponse.toString() +
                "        response.setCreatedAt(entity.getCreatedAt());\n" +
                "        response.setUpdatedAt(entity.getUpdatedAt());\n" +
                "        return response;\n" +
                "    }\n" +
                "}\n";
        writeToFile(modulePath + "\\mapper\\" + moduleNameCap + "Mapper.java", content);
    }

    private static void generateRepository(String modulePath, String modulePackage, String moduleNameCap) throws IOException {
        String content = "package " + modulePackage + ".repository;\n" +
                "import " + modulePackage + ".entity." + moduleNameCap + "Entity;\n" +
                "import org.springframework.data.jpa.repository.JpaRepository;\n" +
                "import org.springframework.stereotype.Repository;\n\n" +
                "@Repository\n" +
                "public interface " + moduleNameCap + "Repository extends JpaRepository<" + moduleNameCap + "Entity, Long> {\n\n" +
                "}\n";
        writeToFile(modulePath + "\\repository\\" + moduleNameCap + "Repository.java", content);
    }

    private static void generateService(String modulePath, String modulePackage, String moduleNameCap) throws IOException {
        String content = "package " + modulePackage + ".service;\n" +
                "import " + modulePackage + ".dto." + moduleNameCap + "Request;\n" +
                "import " + modulePackage + ".dto." + moduleNameCap + "Response;\n" +
                "import java.util.List;\n" +
                "import java.util.Optional;\n\n" +
                "public interface " + moduleNameCap + "Service {\n" +
                "    " + moduleNameCap + "Response create" + moduleNameCap + "(" + moduleNameCap + "Request request);\n" +
                "    List<" + moduleNameCap + "Response> fetch" + moduleNameCap + "();\n" +
                "    Optional<" + moduleNameCap + "Response> findBy" + moduleNameCap + "Id(Long id);\n" +
                "    String delete" + moduleNameCap + "ById(Long id);\n" +
                "}\n";
        writeToFile(modulePath + "\\service\\" + moduleNameCap + "Service.java", content);
    }

    private static void generateServiceImpl(String modulePath, String modulePackage, String moduleNameCap) throws IOException {
        String varName = moduleNameCap.toLowerCase();
        String content = "package " + modulePackage + ".service.impl;\n\n" +
                "import " + modulePackage + ".dto." + moduleNameCap + "Request;\n" +
                "import " + modulePackage + ".dto." + moduleNameCap + "Response;\n" +
                "import " + modulePackage + ".entity." + moduleNameCap + "Entity;\n" +
                "import " + modulePackage + ".mapper." + moduleNameCap + "Mapper;\n" +
                "import " + modulePackage + ".repository." + moduleNameCap + "Repository;\n" +
                "import " + modulePackage + ".service." + moduleNameCap + "Service;\n" +
                "import org.springframework.beans.factory.annotation.Autowired;\n" +
                "import org.springframework.stereotype.Service;\n\n" +
                "import java.util.List;\n" +
                "import java.util.Optional;\n" +
                "import java.util.stream.Collectors;\n\n" +
                "@Service\n" +
                "public class " + moduleNameCap + "ServiceImpl implements " + moduleNameCap + "Service {\n\n" +
                "    @Autowired\n" +
                "    private " + moduleNameCap + "Repository " + varName + "Repository;\n\n" +
                "    @Autowired\n" +
                "    private " + moduleNameCap + "Mapper " + varName + "Mapper;\n\n" +
                "    @Override\n" +
                "    public " + moduleNameCap + "Response create" + moduleNameCap + "(" + moduleNameCap + "Request request) {\n" +
                "        " + moduleNameCap + "Entity entity = " + varName + "Mapper.requestToEntity(request);\n" +
                "        " + moduleNameCap + "Entity savedEntity = " + varName + "Repository.save(entity);\n" +
                "        return " + varName + "Mapper.entityToResponse(savedEntity);\n" +
                "    }\n\n" +
                "    @Override\n" +
                "    public List<" + moduleNameCap + "Response> fetch" + moduleNameCap + "() {\n" +
                "        return " + varName + "Repository.findAll().stream()\n" +
                "                .map(" + varName + "Mapper::entityToResponse)\n" +
                "                .collect(Collectors.toList());\n" +
                "    }\n\n" +
                "    @Override\n" +
                "    public Optional<" + moduleNameCap + "Response> findBy" + moduleNameCap + "Id(Long id) {\n" +
                "        return " + varName + "Repository.findById(id)\n" +
                "                .map(" + varName + "Mapper::entityToResponse);\n" +
                "    }\n\n" +
                "    @Override\n" +
                "    public String delete" + moduleNameCap + "ById(Long id) {\n" +
                "        if (" + varName + "Repository.existsById(id)) {\n" +
                "            " + varName + "Repository.deleteById(id);\n" +
                "            return \"" + moduleNameCap + " deleted successfully\";\n" +
                "        }\n" +
                "        return \"" + moduleNameCap + " not found\";\n" +
                "    }\n" +
                "}\n";
        writeToFile(modulePath + "\\service\\impl\\" + moduleNameCap + "ServiceImpl.java", content);
    }

    private static void generateController(String modulePath, String modulePackage, String moduleNameCap) throws IOException {
        String varName = moduleNameCap.toLowerCase();
        String content = "package " + modulePackage + ".controller;\n\n" +
                "import " + modulePackage + ".dto." + moduleNameCap + "Request;\n" +
                "import " + modulePackage + ".dto." + moduleNameCap + "Response;\n" +
                "import " + modulePackage + ".service." + moduleNameCap + "Service;\n" +
                "import io.swagger.v3.oas.annotations.tags.Tag;\n" +
                "import org.springframework.beans.factory.annotation.Autowired;\n" +
                "import org.springframework.web.bind.annotation.*;\n\n" +
                "import java.util.List;\n" +
                "import java.util.Optional;\n\n" +
                "@RestController\n" +
                "@RequestMapping(\"/" + moduleNameCap.toLowerCase() + "s\")\n" +
                "@Tag(name = \"" + moduleNameCap + "s\")\n" +
                "public class " + moduleNameCap + "Controller {\n\n" +
                "    @Autowired\n" +
                "    private " + moduleNameCap + "Service " + varName + "Service;\n\n" +
                "    @GetMapping\n" +
                "    public List<" + moduleNameCap + "Response> fetchAll" + moduleNameCap + "s(){\n" +
                "        return " + varName + "Service.fetch" + moduleNameCap + "();\n" +
                "    }\n\n" +
                "    @PostMapping\n" +
                "    public " + moduleNameCap + "Response create" + moduleNameCap + "(@RequestBody " + moduleNameCap + "Request request){\n" +
                "        return " + varName + "Service.create" + moduleNameCap + "(request);\n" +
                "    }\n\n" +
                "    @GetMapping(\"/{id}\")\n" +
                "    public Optional<" + moduleNameCap + "Response> get" + moduleNameCap + "ById(@PathVariable Long id){\n" +
                "        return " + varName + "Service.findBy" + moduleNameCap + "Id(id);\n" +
                "    }\n\n" +
                "    @DeleteMapping(\"/{id}\")\n" +
                "    public String delete" + moduleNameCap + "(@PathVariable Long id){\n" +
                "        return " + varName + "Service.delete" + moduleNameCap + "ById(id);\n" +
                "    }\n" +
                "}\n";
        writeToFile(modulePath + "\\controller\\" + moduleNameCap + "Controller.java", content);
    }

    static class FieldDef {
        String name;
        String type;

        public FieldDef(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }
}