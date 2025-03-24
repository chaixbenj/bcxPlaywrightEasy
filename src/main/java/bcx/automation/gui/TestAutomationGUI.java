package bcx.automation.gui;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.tree.*;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.awt.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import bcx.automation.appium.element.MobElement;
import bcx.automation.playwright.PlaywrightBrowser;
import bcx.automation.playwright.element.*;
import bcx.automation.playwright.element.CheckboxGroup;
import bcx.automation.properties.GlobalProp;
import bcx.automation.test.TestContext;
import bcx.automation.playwright.page.BasePage;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.testng.TestNG;

public class TestAutomationGUI extends JFrame {
    private static TestContext testContext = new TestContext();
    private final JTree pageTree;
    private final JPanel elementPanel;
    private final JTextArea testCodeArea;
    private final List<String> testSteps = new ArrayList<>();
    private final List<String> newTestSteps = new ArrayList<>();
    private final Map<String, Class<?>> elementTypes = new HashMap<>();
    private final Map<String, Map<String, String>> elementMethods = new HashMap<>();
    private final Map<String, JComboBox<String>> methodSelectors = new HashMap<>();
    private final Map<String, String> pageVariables = new HashMap<>();

    public TestAutomationGUI() {
        super("Test Automation GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Initialize element types map
        initElementTypes();

        // Create components
        pageTree = new JTree(createPageTreeModel());
        pageTree.setCellRenderer(new PageTreeCellRenderer());
        elementPanel = new JPanel();
        elementPanel.setLayout(new BoxLayout(elementPanel, BoxLayout.Y_AXIS));

        testCodeArea = new JTextArea();
        testCodeArea.setEditable(true);
        testCodeArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        // Set up layout
        JSplitPane leftSplitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(pageTree),
                new JScrollPane(elementPanel)
        );
        leftSplitPane.setDividerLocation(300);

        JSplitPane mainSplitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftSplitPane,
                new JScrollPane(testCodeArea)
        );
        mainSplitPane.setDividerLocation(500);

        add(mainSplitPane);

        // Add buttons panel at the bottom
        JPanel buttonPanel = new JPanel();
        JButton testButton = new JButton("Tester");
        JButton generateButton = new JButton("Générer Test");
        JButton saveButton = new JButton("Enregistrer Test");
        JButton clearButton = new JButton("Effacer Test");
        JButton addReportTitleButton = new JButton("Ajouter un titre dans le rapport");

        generateButton.setMargin(new Insets(0, 0, 0, 0));
        saveButton.setMargin(new Insets(0, 0, 0, 0));
        clearButton.setMargin(new Insets(0, 0, 0, 0));
        addReportTitleButton.setMargin(new Insets(0, 0, 0, 0));

        generateButton.setBackground(Color.GREEN);
        saveButton.setBackground(Color.GREEN);
        clearButton.setBackground(Color.ORANGE);
        addReportTitleButton.setBackground(Color.BLUE);


        buttonPanel.add(testButton);
        buttonPanel.add(generateButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(addReportTitleButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Add event handlers
        pageTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) pageTree.getLastSelectedPathComponent();
            if (node == null) return;

            Object nodeInfo = node.getUserObject();
            if (node.isLeaf() && nodeInfo instanceof ElementInfo) {
                displayElementDetails((ElementInfo) nodeInfo);
            }
        });

        testButton.addActionListener(e -> generateAndRunTest());
        generateButton.addActionListener(e -> generateTestCode(true));
        saveButton.addActionListener(e -> saveTestToFile());
        clearButton.addActionListener(e -> {
            testSteps.clear();
            pageVariables.clear();
            testCodeArea.setText("");
        });

        addReportTitleButton.addActionListener(e -> {
            String title = JOptionPane.showInputDialog(this, "Enter report title:", "Add Report Title", JOptionPane.QUESTION_MESSAGE);
            if (title != null && !title.trim().isEmpty()) {
                newTestSteps.add(String.format("this.getReport().title(\"%s\");", title));
                updateTestCode();
            }
        });

        // Analyze element classes to get available methods
        analyzeElementClasses();
    }

    private void initElementTypes() {
        elementTypes.put("BaseElement", BaseElement.class);
        elementTypes.put("RadioGroup", RadioGroup.class);
        elementTypes.put("CheckboxGroup", CheckboxGroup.class);
        elementTypes.put("Dropdown", Dropdown.class);
        elementTypes.put("MultiDropdown", MultiDropdown.class);
        elementTypes.put("Grid", Grid.class);
        elementTypes.put("Element", MobElement.class);


        // Add more element types as needed
    }

    private void analyzeElementClasses() {
        elementMethods.clear();

        for (Map.Entry<String, Class<?>> entry : elementTypes.entrySet()) {
            String typeName = entry.getKey();
            Class<?> elementClass = entry.getValue();

            Map<String, String> methods = new HashMap<>();
            for (Method method : elementClass.getMethods()) {
                // Skip methods from Object class and getters/setters
                if (method.getDeclaringClass().equals(Object.class) ||
                        (method.getName().startsWith("get") && method.getParameterCount() == 0) ||
                        (method.getName().startsWith("set") && !method.getName().equals("setValue")) ||
                        method.getName().startsWith("is")) {
                    continue;
                }
                String methodName = method.getName();
                Class<?>[] parameterTypes = method.getParameterTypes();
                String methodSignature = methodName + "(" + String.join(", ", Arrays.stream(parameterTypes).map(Class::getSimpleName).toArray(String[]::new)) + ")";


                methods.put(method.getName(), methodSignature);
            }

            elementMethods.put(typeName, methods);
        }
    }

/*    private void analyzeElementClasses() {
        for (Class<?> elementClass : elementClasses) {
            Method[] methods = elementClass.getMethods();
            for (Method method : methods) {
                String methodName = method.getName();
                Class<?>[] parameterTypes = method.getParameterTypes();

                // Créer une représentation de la méthode avec ses types de paramètres
                String methodSignature = methodName + "(" + String.join(", ", Arrays.stream(parameterTypes).map(Class::getSimpleName).toArray(String[]::new)) + ")";

                elementMethods.add(methodSignature);
            }
        }
    }*/

    private DefaultTreeModel createPageTreeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Pages");

        try {
            // Identify the test directory
            String testDir = findTestDirectory();
            if (testDir == null) {
                JOptionPane.showMessageDialog(this,
                        "Could not find test/java/pages directory. Using default classpath scanning.",
                        "Directory Warning",
                        JOptionPane.WARNING_MESSAGE);
                return createPageTreeModelFromClasspath(root);
            }

            // Scan for page classes in the pages directory
            Path pagesPath = Paths.get(testDir, "src", "test", "java", "pages");
            if (!Files.exists(pagesPath)) {
                JOptionPane.showMessageDialog(this,
                        "Pages directory not found: " + pagesPath,
                        "Directory Warning",
                        JOptionPane.WARNING_MESSAGE);
                return createPageTreeModelFromClasspath(root);
            }

            // Create URL for class loading
            String testClassesDir = "target/test-classes";
            URL url = new File(testClassesDir).toURI().toURL();
            URL[] urls = new URL[] { new File(testDir).toURI().toURL(), url };
            URLClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader());

            // Scan for page directories and classes
            scanPageDirectories(pagesPath.toFile(), root, "", classLoader);

            return new DefaultTreeModel(root);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error scanning pages: " + e.getMessage() + ". Using default classpath scanning.",
                    "Scan Error",
                    JOptionPane.ERROR_MESSAGE);
            return createPageTreeModelFromClasspath(root);
        }
    }

    private String findTestDirectory() {
        // Start from current directory and go up trying to find the test/java/pages structure
        Path currentDir = Paths.get("").toAbsolutePath();

        while (currentDir != null) {
            Path testPath = currentDir.resolve(Paths.get("src", "test", "java", "pages"));
            if (Files.exists(testPath) && Files.isDirectory(testPath)) {
                return currentDir.toString();
            }
            currentDir = currentDir.getParent();
        }

        return null;
    }

    private void scanPageDirectories(File dir, DefaultMutableTreeNode parentNode, String packagePath, ClassLoader classLoader) {
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) return;

        // First, add all subdirectories
        for (File file : files) {
            if (file.isDirectory()) {
                String dirName = file.getName();
                DefaultMutableTreeNode dirNode = new DefaultMutableTreeNode(dirName);
                parentNode.add(dirNode);

                String newPackagePath = packagePath.isEmpty() ? dirName : packagePath + "." + dirName;
                scanPageDirectories(file, dirNode, newPackagePath, classLoader);
            }
        }

        // Then add all Java files as potential pages
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".java")) {
                String className = file.getName().replace(".java", "");
                String fullClassName = "pages." + (packagePath.isEmpty() ? className : packagePath + "." + className);

                try {
                    Class<?> pageClass = classLoader.loadClass(fullClassName);
                    //if (BasePage.class.isAssignableFrom(pageClass) && !pageClass.equals(BasePage.class)) {
                        DefaultMutableTreeNode pageNode = new DefaultMutableTreeNode(new PageInfo(className, fullClassName));
                        parentNode.add(pageNode);

                        // Add elements from the page
                        addElementsToPageNode(pageNode, pageClass);
                    //}
                } catch (ClassNotFoundException e) {
                    System.err.println("Could not load class: " + fullClassName);
                } catch (NoClassDefFoundError e) {
                    System.err.println("Class definition not found for: " + fullClassName);
                } catch (Exception e) {
                    System.err.println("Error loading class: " + fullClassName + " - " + e.getMessage());
                }
            }
        }
    }

    private DefaultTreeModel createPageTreeModelFromClasspath(DefaultMutableTreeNode root) {
        // Scan for page classes in the pages package using Reflections
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("pages"))
                .setScanners(new SubTypesScanner(), new FieldAnnotationsScanner())
                .filterInputsBy(new FilterBuilder().include("pages.*")));

        Set<Class<? extends BasePage>> pageClasses = reflections.getSubTypesOf(BasePage.class);

        // Group pages by package
        Map<String, List<Class<? extends BasePage>>> pagesByPackage = pageClasses.stream()
                .collect(Collectors.groupingBy(cls -> {
                    String pkg = cls.getPackage().getName();
                    return pkg.substring(pkg.lastIndexOf(".") + 1);
                }));

        // Add pages to tree
        for (Map.Entry<String, List<Class<? extends BasePage>>> entry : pagesByPackage.entrySet()) {
            String packageName = entry.getKey();
            List<Class<? extends BasePage>> pages = entry.getValue();

            DefaultMutableTreeNode packageNode = new DefaultMutableTreeNode(packageName);
            root.add(packageNode);

            for (Class<? extends BasePage> pageClass : pages) {
                DefaultMutableTreeNode pageNode = new DefaultMutableTreeNode(
                        new PageInfo(pageClass.getSimpleName(), pageClass.getName()));
                packageNode.add(pageNode);

                // Add elements from the page
                addElementsToPageNode(pageNode, pageClass);
            }
        }

        return new DefaultTreeModel(root);
    }

    private void addElementsToPageNode(DefaultMutableTreeNode pageNode, Class<?> pageClass) {
        Object nodeInfo = pageNode.getUserObject();
        if (!(nodeInfo instanceof PageInfo)) return;

        PageInfo pageInfo = (PageInfo) nodeInfo;

        try {
            Constructor<?> constructor = pageClass.getDeclaredConstructor(TestContext.class);
            Object pageInstance = constructor.newInstance(testContext);

            for (Field field : pageClass.getDeclaredFields()) {
                String fieldTypeName = field.getType().getSimpleName();
                if (elementTypes.containsKey(fieldTypeName)) {
                    String elementName = field.getName();
                    field.setAccessible(true);

                    Object fieldInstance = field.get(pageInstance);

                    // Vérifier que l'instance est bien un BaseElement
                    String description = "";
                    if (fieldInstance instanceof BaseElement) {
                        BaseElement baseElement = (BaseElement) fieldInstance;
                        description = baseElement.getName();
                    } else if (fieldInstance instanceof MobElement) {
                        MobElement element = (MobElement) fieldInstance;
                        description = element.getName();
                    }

                    ElementInfo elementInfo = new ElementInfo(
                            elementName,
                            description,
                            fieldTypeName,
                            pageInfo.getName(),
                            pageInfo.getFullClassName()
                    );
                    DefaultMutableTreeNode elementNode = new DefaultMutableTreeNode(elementInfo);
                    pageNode.add(elementNode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        testContext.getPage().close();
    }

    private void displayElementDetails(ElementInfo elementInfo) {
        elementPanel.removeAll();
        // Element info panel
        JPanel infoPanel = new JPanel(new GridLayout(3, 2));
        infoPanel.add(new JLabel("Element Name:"));
        infoPanel.add(new JLabel(elementInfo.getDescription()));
        infoPanel.add(new JLabel("Element Type:"));
        infoPanel.add(new JLabel(elementInfo.getType()));
        infoPanel.add(new JLabel("Page:"));
        infoPanel.add(new JLabel(elementInfo.getPageName()));

        elementPanel.add(infoPanel);

        // Separator
        elementPanel.add(Box.createVerticalStrut(10));
        elementPanel.add(new JSeparator());
        elementPanel.add(Box.createVerticalStrut(10));

        elementPanel.setPreferredSize(new Dimension(elementPanel.getWidth(), 400)); // Hauteur fixe de 400px, ajustez selon vos besoins
        elementPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));


        // Create page instance if needed
        JButton createPageButton = new JButton("Create Page Instance");
        createPageButton.setMargin(new Insets(0, 0, 0, 0));
        createPageButton.setBackground(Color.GREEN);
        createPageButton.addActionListener(e -> {
            String pageVar = getVariableName(elementInfo.getPageName());
            String pageClass = elementInfo.getPageClassName();

            if (!pageVariables.containsKey(pageClass)) {
                String instantiation = String.format("%s %s = new %s(this.testContext);",
                        elementInfo.getPageName(),
                        pageVar,
                        elementInfo.getPageName());

                newTestSteps.add(instantiation);
                pageVariables.put(pageClass, pageVar);
                updateTestCode();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Page instance already created: " + pageVar,
                        "Info",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        elementPanel.add(createPageButton);

        // Navigation
        JButton navigateButton = new JButton("Add Navigation");
        navigateButton.setMargin(new Insets(0, 0, 0, 0));
        navigateButton.setBackground(Color.GREEN);
        navigateButton.addActionListener(e -> {
            String pageVar = getVariableName(elementInfo.getPageName());
            String pageClass = elementInfo.getPageClassName();

            // Create page instance if it doesn't exist
            if (!pageVariables.containsKey(pageClass)) {
                String instantiation = String.format("%s %s = new %s(this.testContext);",
                        elementInfo.getPageName(),
                        pageVar,
                        elementInfo.getPageName());

                newTestSteps.add(instantiation);
                pageVariables.put(pageClass, pageVar);
            }

            String navigation = String.format("%s.navigate();", pageVar);
            newTestSteps.add(navigation);
            updateTestCode();
        });

        elementPanel.add(navigateButton);
        elementPanel.add(Box.createVerticalStrut(10));

        // Method selector and param panel in the same row
        JPanel actionRow = new JPanel(new BorderLayout());

        // Method selector - left side
        JPanel methodPanel = new JPanel(new BorderLayout());
        methodPanel.add(new JLabel("Select Action:"), BorderLayout.NORTH);

        JComboBox<String> methodSelector = new JComboBox<>();
        methodSelector.setBackground(Color.BLUE);
        Map<String, String> methods = elementMethods.get(elementInfo.getType());
        if (methods != null) {
            // Add most common methods first, then alphabetically
            List<String> commonMethods = Arrays.asList("click", "setValue", "assertValue", "assertVisible");

            // Add common methods first
            for (String methodName : commonMethods) {
                if (methods.containsKey(methodName)) {
                    methodSelector.addItem(methodName);
                }
            }

            // Add remaining methods alphabetically
            methods.keySet().stream()
                    .filter(m -> !commonMethods.contains(m))
                    .sorted()
                    .forEach(methodSelector::addItem);
        }

        methodPanel.add(methodSelector, BorderLayout.CENTER);
        actionRow.add(methodPanel, BorderLayout.WEST);

        // Method parameters - right side
        JPanel paramPanel = new JPanel(new BorderLayout());
        paramPanel.add(new JLabel("Parameter:"), BorderLayout.NORTH);

        JTextField paramField = new JTextField(20);
        paramPanel.add(paramField, BorderLayout.CENTER);

        actionRow.add(paramPanel, BorderLayout.CENTER);

        // Add the combined row to the main panel
        elementPanel.add(actionRow);
        elementPanel.add(Box.createVerticalStrut(10));

        // Initialize visibility
        paramPanel.setVisible(false);
        methodSelector.addActionListener(e -> {
            String selectedMethod = (String) methodSelector.getSelectedItem();
            switch (selectedMethod) {
                case "click":
                    paramField.setText("");
                    paramPanel.setVisible(false);
                    break;
                default:
                    paramPanel.setVisible(true);
                    break;
            }
        });

        // Add action button
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addActionButton = new JButton("Add Action");
        addActionButton.setMargin(new Insets(0, 0, 0, 0));
        addActionButton.setBackground(Color.GREEN);
        addActionButton.addActionListener(e -> {
            String selectedMethod = (String) methodSelector.getSelectedItem();
            String param = paramField.getText().trim();
            String pageVar = getVariableName(elementInfo.getPageName());
            String pageClass = elementInfo.getPageClassName();

            // Create page instance if it doesn't exist
            if (!pageVariables.containsKey(pageClass)) {
                String instantiation = String.format("%s %s = new %s(this.testContext);",
                        elementInfo.getPageName(),
                        pageVar,
                        elementInfo.getPageName());

                newTestSteps.add(instantiation);
                pageVariables.put(pageClass, pageVar);
            } else {
                pageVar = pageVariables.get(pageClass);
            }

            if (selectedMethod != null) {
                // Récupérer la signature de la méthode pour obtenir les types de paramètres
                String methodSignature = elementMethods.get(elementInfo.getType()).get(selectedMethod);

                // Extraire le type du paramètre (en supposant un seul paramètre pour simplifier)
                String parameterType = "";
                if (methodSignature.contains("(")) {
                    parameterType = methodSignature.substring(methodSignature.indexOf("(") + 1, methodSignature.indexOf(")"));
                }

                // Formater le paramètre en fonction de son type
                String formattedParam = formatParameter(param, parameterType);

                String action = String.format("%s.%s.%s(%s);",
                        pageVar,
                        elementInfo.getName(),
                        selectedMethod,
                        formattedParam);

                newTestSteps.add(action);
                updateTestCode();
            }
        });

        actionButtonPanel.add(addActionButton);
        elementPanel.add(actionButtonPanel);

        // Add injectValues if type supports it
        if (methods != null && methods.containsKey("injectValues")) {
            JPanel injectPanel = new JPanel(new BorderLayout());
            injectPanel.add(new JLabel("Inject Values (key,value):"), BorderLayout.NORTH);

            JPanel injectFieldsPanel = new JPanel(new GridLayout(1, 2));
            JTextField keyField = new JTextField(10);
            JTextField valueField = new JTextField(10);
            injectFieldsPanel.add(keyField);
            injectFieldsPanel.add(valueField);

            injectPanel.add(injectFieldsPanel, BorderLayout.CENTER);

            JButton injectButton = new JButton("Add injectValues");
            injectButton.setMargin(new Insets(0, 0, 0, 0));
            injectButton.setBackground(Color.GREEN);
            injectButton.addActionListener(e -> {
                String key = keyField.getText().trim();
                String value = valueField.getText().trim();

                if (!key.isEmpty() && !value.isEmpty()) {
                    String pageVar = pageVariables.getOrDefault(elementInfo.getPageClassName(),
                            getVariableName(elementInfo.getPageName()));

                    String action = String.format("%s.%s.injectValues(\"%s\", \"%s\");",
                            pageVar,
                            elementInfo.getName(),
                            key,
                            value);

                    newTestSteps.add(action);
                    updateTestCode();
                }
            });
            injectPanel.add(injectButton, BorderLayout.SOUTH);
            elementPanel.add(injectPanel);
        }

        // Store method selector for this element
        methodSelectors.put(elementInfo.getName(), methodSelector);

        elementPanel.revalidate();
        elementPanel.repaint();
    }

    // Méthode pour formater le paramètre en fonction de son type
    private String formatParameter(String param, String parameterType) {
        if (param.isEmpty()) {
            return "";
        }

        switch (parameterType.toLowerCase()) {
            case "boolean":
                return param; // pas de guillemets pour les booléens
            case "int":
            case "long":
            case "float":
            case "double":
                return param; // pas de guillemets pour les types numériques
            default:
                return "\"" + param + "\""; // guillemets pour les autres types (comme String)
        }
    }

    private String getVariableName(String className) {
        if (className == null || className.isEmpty()) {
            return "";
        }
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    private void updateTestCode() {
        // Créer la chaîne de caractères qui sera ajoutée
        StringBuilder codeBuilder = new StringBuilder();

        for (String step : newTestSteps) {
            codeBuilder.append("            ").append(step).append("\n");
        }

        // Obtenez la position actuelle du curseur
        int caretPosition = testCodeArea.getCaretPosition();

        // Ajoutez le code à la position actuelle du curseur
        try {
            testCodeArea.getDocument().insertString(caretPosition, codeBuilder.toString(), null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        // Réinitialisez la position du curseur à la fin du texte
        testCodeArea.setCaretPosition(caretPosition + codeBuilder.length());
        newTestSteps.clear();

        reloadTestSteps();

    }

    private void reloadTestSteps() {
        testSteps.clear();
        for (String line : testCodeArea.getText().split("\\n")) {
            testSteps.add(line.trim());
        }
    }

    private void generateAndRunTest() {
        try {
            // Supprimer le répertoire allure-results
            deleteDirectory(new File("target/allure-results"));

            // Générer le code de test
            String testCode = generateTestCode(false);

            File tempDir = new File("target/test-classes/testCase/temp_test_" + System.currentTimeMillis());
            tempDir.mkdir();

            // Écrire le code dans un fichier temporaire
            String testClassName = "TempTest" + System.currentTimeMillis();
            testCode = testCode.replace("GeneratedTest", testClassName).replace("testCase", tempDir.getName());
            File tempFile = new File(tempDir, testClassName + ".java");
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(testCode);
            }

            // Compiler le fichier
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

            // Définir le classpath pour inclure toutes les dépendances nécessaires
            String classpath = System.getProperty("java.class.path") + File.pathSeparator + "target/classes" + File.pathSeparator + "target/test-classes";

            Iterable<String> options = Arrays.asList("-classpath", classpath);

            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(tempFile));
            compiler.getTask(null, fileManager, null, options, null, compilationUnits).call();

            // Charger et exécuter la classe de test
            String testClassesDir = "target/test-classes";
            URL urlTestClassesDir = new File(testClassesDir).toURI().toURL();
            String classesDir = "target/classes";
            URL urlClassesDir = new File(classesDir).toURI().toURL();
            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{tempDir.toURI().toURL(), urlClassesDir, urlTestClassesDir});
            Class<?> testClass = Class.forName(testClassName, true, classLoader);

            System.setProperty("allure.results.directory", "target/allure-results");
            System.setProperty("env", "PROD");

            TestNG testng = new TestNG();
            testng.setTestClasses(new Class[] { testClass });
            testng.run();

            // Lancer Allure pour afficher le rapport
            openAllureReport();

            // Nettoyer les fichiers temporaires
            deleteDirectory(tempDir);

            // Supprimer le répertoire test-output
            File testOutputDir = new File("test-output");
            deleteDirectory(testOutputDir);

            JOptionPane.showMessageDialog(this, "Test exécuté. Le rapport est en cours de construction.");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de l'exécution du test : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openAllureReport() {
        Thread reportThread = new Thread(() -> {
            try {
                Process process = Runtime.getRuntime().exec("mvn.cmd allure:serve");

                // Optionnel : Lire la sortie du processus
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }

                // Attendre que le processus se termine
                process.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        reportThread.start();
    }


    private static void deleteDirectory(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }

    private String generateTestCode(boolean printInTextArea) {
        reloadTestSteps();
        String testName = "GeneratedTest";
        String packageName = "testCase";
        if (printInTextArea) {
            testName = JOptionPane.showInputDialog(this, "Enter test class name:", "Generate Test", JOptionPane.QUESTION_MESSAGE);
            if (testName == null || testName.trim().isEmpty()) {
                testName = "GeneratedTest";
            }

            // Ask for package name
            packageName = JOptionPane.showInputDialog(this, "Enter package name:", "testCase.Generated", JOptionPane.QUESTION_MESSAGE);
            if (packageName == null || packageName.trim().isEmpty()) {
                packageName = "testCase.Generated";
            }
        }

        StringBuilder codeBuilder = new StringBuilder();

        // Package and imports
        if (printInTextArea) codeBuilder.append("package ").append(packageName).append(";\n\n");
        codeBuilder.append("import bcx.automation.test.BaseTest;\n");
        codeBuilder.append("import bcx.automation.report.Reporter;\n");
        codeBuilder.append("import bcx.automation.util.data.DataUtil;\n");
        codeBuilder.append("import io.qameta.allure.*;\n");
        codeBuilder.append("import org.testng.annotations.Test;\n");

        // Collect all used page classes
        Set<String> usedPageClasses = new HashSet<>();
        for (String pageClassName : pageVariables.keySet()) {
            usedPageClasses.add(pageClassName);
        }

        // Add imports for used pages
        for (String pageClass : usedPageClasses) {
            codeBuilder.append("import ").append(pageClass).append(";\n");
        }

        codeBuilder.append("\n\npublic class ").append(testName).append(" extends BaseTest {\n");
        codeBuilder.append("    @Test(groups = {\"generated\"})\n");
        codeBuilder.append("    @Epic(\"Generated Test\")\n");
        codeBuilder.append("    @Feature(\"Automated Test\")\n");
        codeBuilder.append("    @Description(\"This test was generated using the Test Automation GUI.\")\n");
        codeBuilder.append("    @Severity(SeverityLevel.NORMAL)\n");
        codeBuilder.append("    public void run() {\n");
        codeBuilder.append("        try {\n");

        // Add random data generation if needed
        boolean needsRandomData = testSteps.stream().anyMatch(step -> step.contains("DataUtil"));
        if (needsRandomData) {
            codeBuilder.append("            String randomString = DataUtil.randomAlphaString().toLowerCase();\n");
            codeBuilder.append("            String randomEmail = randomString + \"@\" + randomString + \".com\";\n");
            codeBuilder.append("            String randomPassword = randomString + \"A1!\";\n\n");
        }

        // Add test steps
        if (!testSteps.isEmpty()) {
            // If no report title is set, add a default one
            if (testSteps.stream().noneMatch(step -> step.contains("getReport().title"))) {
                codeBuilder.append("            this.getReport().title(\"Generated Test Steps\");\n");
            }

            for (String step : testSteps) {
                codeBuilder.append("            ").append(step).append("\n");
            }
        }

        // Close the test method
        codeBuilder.append("        } catch (Exception e) {\n");
        codeBuilder.append("            this.getReport().log(Reporter.FAIL_STATUS, e);\n");
        codeBuilder.append("        } finally {\n");
        codeBuilder.append("            endTest();\n");
        codeBuilder.append("        }\n");
        codeBuilder.append("    }\n");
        codeBuilder.append("}\n");

        if (printInTextArea) testCodeArea.setText(codeBuilder.toString());
        return codeBuilder.toString();
    }

    private List<DefaultMutableTreeNode> getAllNodes(DefaultMutableTreeNode root) {
        List<DefaultMutableTreeNode> nodes = new ArrayList<>();
        Enumeration<?> e = root.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            nodes.add((DefaultMutableTreeNode) e.nextElement());
        }
        return nodes;
    }

    private void saveTestToFile() {
        if (testCodeArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No test code to save. Please generate a test first.",
                    "No Test Code",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Test File");
        fileChooser.setSelectedFile(new File("GeneratedTest.java"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            // Add .java extension if not present
            if (!fileToSave.getName().toLowerCase().endsWith(".java")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".java");
            }

            try (FileWriter writer = new FileWriter(fileToSave)) {
                writer.write(testCodeArea.getText());
                JOptionPane.showMessageDialog(this,
                        "Test saved successfully to " + fileToSave.getAbsolutePath(),
                        "Save Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error saving test: " + e.getMessage(),
                        "Save Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        try {
            GlobalProp.load();
            PlaywrightBrowser.startNewBrowser(testContext);
            //testContext.setAppiumDriver(new AppiumDriver("", ""));
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            TestAutomationGUI gui = new TestAutomationGUI();
            gui.setVisible(true);
        });
    }

    // Helper class to store element information
    private static class ElementInfo {
        private final String name;
        private final String description;
        private final String type;
        private final String pageName;
        private final String pageClassName;

        public ElementInfo(String name, String description, String type, String pageName, String pageClassName) {
            this.name = name;
            this.description = description;
            this.type = type;
            this.pageName = pageName;
            this.pageClassName = pageClassName;
        }

        public String getName() {
            return name;
        }
        public String getDescription() {
            return description;
        }

        public String getType() {
            return type;
        }

        public String getPageName() {
            return pageName;
        }

        public String getPageClassName() {
            return pageClassName;
        }

        @Override
        public String toString() {
            return name + " (" + type + ")";
        }
    }

    // Helper class to store page information
    private static class PageInfo {
        private final String name;
        private final String fullClassName;

        public PageInfo(String name, String fullClassName) {
            this.name = name;
            this.fullClassName = fullClassName;
        }

        public String getName() {
            return name;
        }

        public String getFullClassName() {
            return fullClassName;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // Custom cell renderer for the page tree
    private static class PageTreeCellRenderer extends DefaultTreeCellRenderer {
        private final Icon folderIcon = UIManager.getIcon("FileView.directoryIcon");
        private final Icon pageIcon = UIManager.getIcon("FileView.fileIcon");
        private final Icon elementIcon = UIManager.getIcon("Tree.leafIcon");

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();

            if (userObject instanceof ElementInfo) {
                ElementInfo elementInfo = (ElementInfo) userObject;
                setText(elementInfo.getDescription() + " (" + elementInfo.getType() + ")");
                try {
                    setIcon(elementIcon);
                } catch (Exception e) {
                    // Fallback to default icon
                    setIcon(UIManager.getIcon("Tree.leafIcon"));
                }
            } else if (userObject instanceof PageInfo) {
                setIcon(pageIcon);
            } else if (!leaf) {
                setIcon(folderIcon);
            }

            return this;
        }
    }
}