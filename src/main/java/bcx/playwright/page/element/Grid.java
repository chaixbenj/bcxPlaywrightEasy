package bcx.playwright.page.element;

import bcx.playwright.test.TestContext;
import com.microsoft.playwright.Locator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import bcx.playwright.report.Reporter;
import bcx.playwright.util.data.DataUtil;
import bcx.playwright.util.data.DoubleUtil;
import bcx.playwright.properties.GlobalProp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * classe permettant de gérer les action sur des grilles de type table/thead/tboby
 * Ne pas modifier dans le cadre d'une appli : modifier la classe Grid qui en hérite
 * @author bcx
 *
 */
@Slf4j
public class Grid extends BaseElement {
    public static final String POTENTIAL_TYPE = "table";
    private static final int TIME_OUT_ASSERTION = 5;
    public static final String HEADER_NUM = "{HEADER_NUM}";
    public static final String COL_NUMBER = "{COL_NUMBER}";
    public static final String HEADER_ROW_NUM = "{HEADER_ROW_NUM}";
    public static final String HEADER_NAME = "{HEADER_NAME}";
    public static final String VALUE_IN_ROW = "{VALUE_IN_ROW}";
    public static final String ROW_NUMBER = "{ROW_NUMBER}";
    public static final String CELLS_VALUES = "{CELLS_VALUES}";
    public static final String ROW_CONTENT = "{ROW_CONTENT}";
    public static final String BUILT_VALUES = "{BUILT_VALUES}";
    public static final String ATTR_ACTION = "{ATTR_ACTION}";
    private static final String COL_NOT_FOUND = "col not found";
    public static final String SUR_COL = " sur col ";
    public static final String CELLULE = "cellule ";
    public static final String CONTENANT = " contenant ";
    public static final String NON_TROUVEE = " non trouvée";
    public static final String COL = " col ";
    public static final String TBODY_TR_TD = "//tbody/tr[td][";

    private final HashMap<String, Integer> headerNamePosition = new HashMap<>();
    private int headerRowNum = 0;


    @Getter
    private final BaseElement table = new BaseElement(this.getTestContext(), "table", this.getPage().locator("//tr/ancestor::table")).setContainer(this);
    @Getter
    private final BaseElement tableTheadRows = new BaseElement(this.getTestContext(), "lignes de header", this.getPage().locator("//thead/tr")).setContainer(this);
    @Getter
    private final BaseElement tableTheadRowN = new BaseElement(this.getTestContext(), "headers des lignes "+HEADER_ROW_NUM, this.getPage().locator("//thead/tr["+HEADER_ROW_NUM+"]/th")).setContainer(this);
    @Getter
    private final BaseElement tableHeaders = new BaseElement(this.getTestContext(), "headers des lignes de données", this.getPage().locator("//thead/tr/th")).setContainer(this);
    @Getter
    private final BaseElement oneHeaderByName = new BaseElement(this.getTestContext(), "header des lignes de données "+HEADER_NAME, this.getPage().locator("//thead/tr/th[contains(., \""+HEADER_NAME+"\")]|//thead/tr/th[attribute::*[contains(., \"HEADER_NAME\")]]")).setContainer(this);
    @Getter
    private final BaseElement oneHeaderByNum = new BaseElement(this.getTestContext(), "header des lignes de données "+HEADER_NUM, this.getPage().locator("//thead/tr/th["+ HEADER_NUM +"]")).setContainer(this);

    @Getter
    private final BaseElement tableRows = new BaseElement(this.getTestContext(), "liste des lignes de données", this.getPage().locator("//tbody//tr[td][not(contains(., \"Aucun résultat\"))]")).setContainer(this);
    @Getter
    private final BaseElement oneRowContainingOneValue = new BaseElement(this.getTestContext(), "ligne de données contenant "+VALUE_IN_ROW, this.getPage().locator("//tbody/tr[td][contains(., \""+VALUE_IN_ROW+"\")]")).setContainer(this);
    @Getter
    private final BaseElement oneRowByNum = new BaseElement(this.getTestContext(), "ligne de données "+ROW_NUMBER, this.getPage().locator(TBODY_TR_TD +ROW_NUMBER+"]")).setContainer(this);
    @Getter
    private final BaseElement oneRowByCellsValues = new BaseElement(this.getTestContext(), "ligne de données contenant "+CELLS_VALUES, this.getPage().locator("//tbody/tr"+BUILT_VALUES)).setContainer(this);

    @Getter
    private final BaseElement oneCellByRowNumColNum = new BaseElement(this.getTestContext(), CELLULE +COL_NUMBER+" de la ligne "+ROW_NUMBER, this.getPage().locator(TBODY_TR_TD +ROW_NUMBER+"]/td["+COL_NUMBER+"]")).setContainer(this);
    @Getter
    private final BaseElement oneCellByCellsValuesColNum = new BaseElement(this.getTestContext(), "cellule colonne "+COL_NUMBER+" de la ligne contenant "+CELLS_VALUES, this.getPage().locator("//tbody/tr"+BUILT_VALUES+"/td["+COL_NUMBER+"]")).setContainer(this);
    @Getter
    private final BaseElement oneCellContainingOneValueByColNum = new BaseElement(this.getTestContext(), CELLULE +COL_NUMBER+" de la ligne "+ROW_CONTENT, this.getPage().locator("//tbody/tr[td][contains(., \""+ROW_CONTENT+"\")]/td["+COL_NUMBER+"]")).setContainer(this);

    @Getter
    private final BaseElement oneColumnByNum = new BaseElement(this.getTestContext(), "liste des cellules de la colonne "+COL_NUMBER, this.getPage().locator("(//tbody/tr[td])/td["+COL_NUMBER+"]")).setContainer(this);
    @Getter
    private final BaseElement actionElementInRow = new BaseElement(this.getTestContext(), "action "+ATTR_ACTION+" sur la ligne", this.getPage().locator("//*[attribute::*[contains(., \""+ATTR_ACTION+"\")]]"));
    @Getter
    private final BaseElement actionElementInRowByNum = new BaseElement(this.getTestContext(), "action "+ATTR_ACTION+" sur la ligne "+ROW_NUMBER, this.getPage().locator(TBODY_TR_TD +ROW_NUMBER+"]//*[attribute::*[contains(., \""+ATTR_ACTION+"\")]]")).setContainer(this);
    @Getter
    private final BaseElement oneFooterByColNum = new BaseElement(this.getTestContext(), CELLULE +COL_NUMBER+" du footer", this.getPage().locator("//tfoot/tr[td]/td["+COL_NUMBER+"]")).setContainer(this);




    /**
     * constructeur de l'élément
     * @param testContext    : contexte de test
     * @param name : nom de l'élément
     * @param locator : locator de l'élément
     */
    public Grid(TestContext testContext, String name, Locator locator) {
        super(testContext, name, locator);
        this.setPotentialType(POTENTIAL_TYPE);
    }


    /**
     * renvoi le numéro de la colonne d'entête headerName
     * @param headerName
     * @return
     */
    public int getColNumber(String headerName) {
        int headersCount = tableHeaders.count();
        for (int i = 0; i < headersCount; i++) {
                String headerText = tableHeaders.getLocator().nth(i).innerText();
            if (headerText.equals(headerName)) {
                return i + 1;
            }
        }
        this.getTestContext().getReport().log(Reporter.WARNING_STATUS_NO_SCREENSHOT, "Colonne " + headerName + NON_TROUVEE);
        return 0;
    }


    /**
     * renvoi le premier numéro de ligne contenant le texte subStringInRow
     * @param subStringInRow
     * @return
     */
    public int getRowNumber(String subStringInRow) {
        int rowCount = tableRows.count();
        for (int i = 0; i < rowCount; i++) {
            String rowText = tableRows.getLocator().nth(i).innerText();
            if (rowText.contains(subStringInRow)) {
                return i + 1;
            }
        }
        this.getTestContext().getReport().log(Reporter.WARNING_STATUS_NO_SCREENSHOT, "Ligne contenant " + subStringInRow + NON_TROUVEE);
        return 0;
    }

    /**
     * renvoi le premier numéro de ligne contenant le texte subStringInCell dans la colonne d'entête headerName
     * @param subStringInCell
     * @param headerName
     * @return
     */
    public int getRowNumber(String subStringInCell, String headerName) {
        return getRowNumber(subStringInCell, getColNumber(headerName));
    }

    /**
     * renvoi le premier numéro de ligne contenant le texte subStringInCell dans la colonne numéro colNumber
     * @param subStringInCell
     * @param colNumber
     * @return
     */
    public int getRowNumber(String subStringInCell, int colNumber) {
        if (colNumber>0) {
            BaseElement oneCol = oneColumnByNum.injectValues(COL_NUMBER, String.valueOf(colNumber));
            int rowCount = oneCol.count();
            for (int i = 0; i < rowCount; i++) {
                String rowText = oneCol.getLocator().nth(i).innerText();
                if (rowText.contains(subStringInCell)) {
                    return i + 1;
                }
            }
        }
       this.getTestContext().getReport().log(Reporter.WARNING_STATUS_NO_SCREENSHOT, "Ligne contenant " + subStringInCell + " dans la colonne "  + colNumber + NON_TROUVEE);
        return 0;
    }


    /**
     * Renvoi le nombre de ligne.
     * @return nombre de ligne
     */
    public int getRowCount() {
        return tableRows.count();
    }

    /**
     * Indique si la table est displayed.
     * @return true si la table est visible, false sinon
     */
    public boolean exists() {
        return tableHeaders.exists();
    }

    /**
     * Indique si la table contient une valeur dans un délai de timeout.
     * @return true si la table est oui, false sinon
     */
    public boolean contains(String value) {
        return oneRowContainingOneValue.injectValues(VALUE_IN_ROW, value).exists();
    }

    /**
     * Indique si une ligne de la table contient une valeur dans une colonne.
     * @param subStringInCell valeur que l'on cherche dans la colonne subStringHeaderName
     * @param subStringHeaderName nom de la colonne dans laquelle on cherche subStringInCell
     * @return true si la ligne existe sinon false
     */
    public boolean columnContains(String subStringInCell, String subStringHeaderName) {
        return columnContains(subStringInCell, getColNumber(subStringHeaderName));
    }

    /**
     * Indique si une ligne de la table contient une valeur dans une colonne.
     * @param subStringInCell valeur que l'on cherche dans la colonne columnNumber
     * @param columnNumber numero de la colonne où on recherche la valeur
     * @return true si la ligne existe sinon false
     */
    public boolean columnContains(String subStringInCell, int columnNumber) {
        boolean foundRow = false;
        try {
            if (columnNumber>0) {
                BaseElement oneCol = oneColumnByNum.injectValues(COL_NUMBER, String.valueOf(columnNumber));
                for (int i = 0; i < oneCol.count(); i++) {
                    String rowText = oneCol.getLocator().nth(i).innerText();
                    if (rowText.contains(subStringInCell)) {
                        foundRow = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // on passe à la suite
        }
        return foundRow;
    }


    /**
     * renvoi le texte complet de la ligne rowNum
     * @param rowNum
     * @return
     */
    public String getRowValue(int rowNum) {
        return oneRowByNum.injectValues(ROW_NUMBER, String.valueOf(rowNum)).getValue();
    }

    /**
     * renvoi l'élément TR de la ligne rowNum
     * @param rowNum
     * @return
     */
     public BaseElement getRow(int rowNum) {
        return oneRowByNum.injectValues(ROW_NUMBER, String.valueOf(rowNum));
    }

    /**
     * renvoi l'élément TR de la ligne contenant subStringInRow
     * @param subStringInRow
     * @return
     */
     public BaseElement getRow(String subStringInRow) {
        return oneRowContainingOneValue.injectValues(VALUE_IN_ROW, String.valueOf(subStringInRow));
    }

    /**
     * renvoi l'élément TR de la ligne contenant les valeurs cellsValues
     * @param cellsValues
     * @return
     */
     public BaseElement getRow(String[] cellsValues) {
        String[] builtXpath = buildXpathFindRowOrCell(cellsValues);
        String sCellsValues = builtXpath[0];
        String builtValuesXpath = builtXpath[1];
        return oneRowByCellsValues.injectValues(Map.of(CELLS_VALUES, sCellsValues, BUILT_VALUES, builtValuesXpath));
    }

    /**
     * renvoi l'élément TD de la colonne headerName de la ligne contenant subStringInRow
     * @param subStringInRow
     * @param headerName
     * @return
     */
     public BaseElement getCell(String subStringInRow, String headerName) {
        int columnNumber = getColNumber(headerName);
        return getCell(subStringInRow, columnNumber);
    }

    /**
     * renvoi l'élément TD de la colonne headerName de la ligne contenant subStringInRow
     * @param rowNum
     * @param headerName
     * @return
     */
     public BaseElement getCell(int rowNum, String headerName) {
        int columnNumber = getColNumber(headerName);
        return oneCellByRowNumColNum.injectValues(Map.of(ROW_NUMBER, String.valueOf(rowNum) , COL_NUMBER, String.valueOf(columnNumber)));
    }

    /**
     * renvoi l'élément TD de la colonne columnNumber de la ligne contenant subStringInRow
     * @param subStringInRow
     * @param columnNumber
     * @return
     */
     public BaseElement getCell(String subStringInRow, int columnNumber) {
        return oneCellByRowNumColNum.injectValues(Map.of(ROW_NUMBER, String.valueOf(getRowNumber(subStringInRow)) , COL_NUMBER, String.valueOf(columnNumber)));
    }

    /**
     * renvoi l'élément TD de la colonne columnNumber de la ligne contenant subStringInRow
     * @param rowNum
     * @param columnNumber
     * @return
     */
     public BaseElement getCell(int rowNum, int columnNumber) {
        return oneCellByRowNumColNum.injectValues(Map.of(ROW_NUMBER, String.valueOf(rowNum) , COL_NUMBER, String.valueOf(columnNumber)));
    }

    /**
     * renvoi l'élément TD de la colonne colNumber de la ligne contenant subStringInCell dans la colonne colNumberSubString
     * @param subStringInCell
     * @param colNumberSubString
     * @param colNumber
     * @return
     */
     public BaseElement getCell(String subStringInCell, int colNumberSubString, int colNumber) {
        return oneCellByRowNumColNum.injectValues(Map.of(ROW_NUMBER, String.valueOf(getRowNumber(subStringInCell, colNumberSubString)) , COL_NUMBER, String.valueOf(colNumber)));
    }

    /**
     * renvoi l'élément TD de la colonne headerName de la ligne contenant les valeurs cellsValues
     * @param cellsValues
     * @param headerName
     * @return
     */
     public BaseElement getCell(String[] cellsValues, String headerName) {
        int columnNumber = getColNumber(headerName);
        return getCell(cellsValues, columnNumber);
    }

    /**
     * renvoi l'élément TD de la colonne columnNumber de la ligne contenant les valeurs cellsValues
     * @param cellsValues
     * @param columnNumber
     * @return
     */
     public BaseElement getCell(String[] cellsValues, int columnNumber) {
        String[] builtXpath = buildXpathFindRowOrCell(cellsValues);
        String sCellsValues = builtXpath[0];
        String builtValuesXpath = builtXpath[1];
        return oneCellByCellsValuesColNum.injectValues(Map.of(CELLS_VALUES, sCellsValues, BUILT_VALUES, builtValuesXpath, COL_NUMBER, String.valueOf(columnNumber)));
    }

    /**
     * recupère la valeur d'une cellule de la colonne headerName de la ligne de la table contenant la chaine subStringInRow.
     * @param subStringInRow chaine pour identifier la ligne
     * @param headerName colonne de la cellule dont on veut la valeur
     * @return la valeur si Reporter.PASS_STATUS, sinon "row not found" ou COL_NOT_FOUND
     */
    public String getCellValue(String subStringInRow, String headerName) {
        return getCellValue(subStringInRow, getColNumber(headerName));
    }

    /**
     * recupère la valeur d'une cellule de la colonne columnNumber de la ligne de la table contenant la chaine subStringInRow.
     * @param columnNumber
     * @param columnNumber colonne de la cellule dont on veut la valeur
     * @return la valeur si Reporter.PASS_STATUS, sinon "row not found" ou COL_NOT_FOUND
     */
    public String getCellValue(String subStringInRow, int columnNumber) {
        return DataUtil.normalizeSpace(getCell(subStringInRow, columnNumber).getValue());
    }


    /**
     * recupère la valeur d'une cellule de la colonne headerName de la ligne de la table contenant la chaine subStringInRow.
     * @param subStringsInRow listes de chaines pour identifier la ligne
     * @param headerName colonne de la cellule dont on veut la valeur
     * @return la valeur si Reporter.PASS_STATUS, sinon "row not found" ou COL_NOT_FOUND
     */
    public String getCellValue(String[] subStringsInRow, String headerName) {
        return getCellValue(subStringsInRow, getColNumber(headerName));
    }

    /**
     * recupère la valeur d'une cellule de la colonne columnNumber de la ligne de la table contenant la chaine subStringInRow.
     * @param subStringsInRow listes de chaines pour identifier la ligne
     * @param columnNumber colonne de la cellule dont on veut la valeur
     * @return la valeur si Reporter.PASS_STATUS, sinon "row not found" ou COL_NOT_FOUND
     */
    public String getCellValue(String[] subStringsInRow, int columnNumber) {
        return DataUtil.normalizeSpace(getCell(subStringsInRow, columnNumber).getValue());
    }
    /**
     * recupère la valeur d'une cellule de la colonne headerName de la ligne de la table contenant la chaine subStringInRow.
     * @param headerName colonne de la cellule dont on veut la valeur
     * @return la valeur si Reporter.PASS_STATUS, sinon "row not found" ou COL_NOT_FOUND
     */
    public String getFooterCellValue(String headerName) {
        return getFooterCellValue(getColNumber(headerName));
    }

    /**
     * recupère la valeur d'une cellule de la colonne columnNumber de la ligne de la table contenant la chaine subStringInRow.
     * @param columnNumber
     * @param columnNumber colonne de la cellule dont on veut la valeur
     * @return la valeur si Reporter.PASS_STATUS, sinon "row not found" ou COL_NOT_FOUND
     */
    public String getFooterCellValue(int columnNumber) {
        String value;
        try {
            if (columnNumber>0) {
                value = oneFooterByColNum.injectValues(COL_NUMBER, String.valueOf(columnNumber)).getValue();
            } else {
                value = COL_NOT_FOUND;
            }
        } catch (Exception e) {
            value = e.getMessage();
        }
        return DataUtil.normalizeSpace(value);
    }
    /**
     * indique si la valeur de la colonne headerName de la ligne contenant subStringInRow vaut cellValue.
     * @param subStringInRow chaine pour identifier la ligne
     * @param headerName colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     * @return true si égalité, false sinon
     */
    public boolean cellValueEquals(String subStringInRow, String headerName, String cellValue) {
        String value = this.getCellValue(subStringInRow, headerName);
        return value.equals(DataUtil.normalizeSpace(cellValue));
    }

    /**
     * indique si la valeur de la colonne colNumber de la ligne contenant subStringInRow vaut cellValue.
     * @param colNumber colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     * @return true si égalité, false sinon
     */
    public boolean cellValueEquals(String subStringInRow, int colNumber, String cellValue) {
        String value = this.getCellValue(subStringInRow, colNumber);
        return value.equals(DataUtil.normalizeSpace(cellValue));
    }

    /**
     * indique si la valeur de la colonne headerName de la ligne contenant subStringInRow contient cellValue.
     * @param subStringInRow chaine pour identifier la ligne
     * @param headerName colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     * @return true si contenue, false sinon
     */
    public boolean cellValueContains(String subStringInRow, String headerName, String cellValue) {
        String value = this.getCellValue(subStringInRow, headerName);
        return value.contains(DataUtil.normalizeSpace(cellValue));
    }

    /**
     * indique si la valeur de la colonne colNumber de la ligne contenant subStringInRow contient cellValue.
     * @param subStringInRow chaine pour identifier la ligne
     * @param colNumber colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     * @return true si contenue, false sinon
     */
    public boolean cellValueContains(String subStringInRow, int colNumber, String cellValue) {
        String value = this.getCellValue(subStringInRow, colNumber);
        return value.contains(DataUtil.normalizeSpace(cellValue));
    }

    /**
     * recupère la valeur d'une cellule de la colonne cellToReadHeaderName de la ligne de la table dont la colonne subStringHeaderName contient la chaine subStringInCell.
     * @param subStringInCell chaine pour identifier la ligne
     * @param subStringHeaderName colonne de la ligne qui doit contenir subStringInCell
     * @param cellToReadHeaderName colonne de la cellule dont on veut la valeur
     * @return la valeur si Reporter.PASS_STATUS, sinon "row not found" ou COL_NOT_FOUND
     */
    public String getCellValue(String subStringInCell, String subStringHeaderName, String cellToReadHeaderName) {
        return getCellValue(subStringInCell, getColNumber(subStringHeaderName), getColNumber(cellToReadHeaderName));
    }

    /**
     * recupère la valeur d'une cellule de la colonne colNumberCellToRead de la ligne de la table dont la colonne colNumber contient la chaine subStringInCell.
     * @param subStringInCell chaine pour identifier la ligne
     * @param colNumber colonne de la ligne qui doit contenir subStringInCell
     * @param colNumberCellToRead colonne de la cellule dont on veut la valeur
     * @return la valeur si Reporter.PASS_STATUS, sinon "row not found" ou COL_NOT_FOUND
     */
    public String getCellValue(String subStringInCell, int colNumber, int colNumberCellToRead) {
        return DataUtil.normalizeSpace(getCell(subStringInCell, colNumber, colNumberCellToRead).getValue());
    }

    /**
     * recupère la valeur de la cellule ligne rowNumber, colonne colNumber.
     * @param rowNumber
     * @param colNumber
     * @return la valeur si Reporter.PASS_STATUS, sinon message d'erreur
     */
    public String getCellValue(int rowNumber, int colNumber) {
        return DataUtil.normalizeSpace(getCell(rowNumber, colNumber).getValue());
    }

    /**
     * indique si la valeur de cellule de la colonne cellToReadHeaderName de la ligne de la table dont la colonne subStringHeaderName contient la chaine subStringInCell vaut cellValue.
     * @param subStringInCell chaine pour identifier la ligne
     * @param subStringHeaderName colonne de la ligne qui doit contenir subStringInCell
     * @param cellToReadHeaderName colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     * @return true si égalité, false sinon
     */
    public boolean cellValueEquals(String subStringInCell, String subStringHeaderName, String cellToReadHeaderName, String cellValue) {
        String value =  getCellValue(subStringInCell, subStringHeaderName, cellToReadHeaderName);
        return value.equals(DataUtil.normalizeSpace(cellValue)) || DoubleUtil.asNum(value).equals(DoubleUtil.asNum(cellValue));
    }

    /**
     * indique si la valeur de cellule de la colonne colNumberCellToRead de la ligne de la table dont la colonne colNumberSubString contient la chaine subStringInCell vaut cellValue.
     * @param subStringInCell chaine pour identifier la ligne
     * @param colNumberSubString colonne de la ligne qui doit contenir subStringInCell
     * @param colNumberCellToRead colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     * @return true si égalité, false sinon
     */
    public boolean cellValueEquals(String subStringInCell, int colNumberSubString, int colNumberCellToRead, String cellValue) {
        String value = this.getCellValue(subStringInCell, colNumberSubString, colNumberCellToRead);
        return value.equals(DataUtil.normalizeSpace(cellValue)) || DoubleUtil.asNum(value).equals(DoubleUtil.asNum(cellValue));
    }

    /**
     * recupère la valeur de l'attribut attr d'une cellule de la colonne cellToReadHeaderName de la ligne de la table dont la colonne subStringHeaderName contient la chaine subStringInCell.
     * @param subStringInCell chaine pour identifier la ligne
     * @param subStringHeaderName colonne de la ligne qui doit contenir subStringInCell
     * @param cellToReadHeaderName colonne de la cellule dont on veut la valeur
     * @param attr attribut dont on veut la valeur
     * @return la valeur de l'attribut si Reporter.PASS_STATUS, sinon "row not found" ou COL_NOT_FOUND
     */
    public String getCellAttribute(String subStringInCell, String subStringHeaderName, String cellToReadHeaderName, String attr) {
        return getCellAttribute(subStringInCell, getColNumber(subStringHeaderName), getColNumber(cellToReadHeaderName), attr);
    }

    /**
     * recupère la valeur de l'attribut attr d'une cellule de la colonne colNumberCellToRead de la ligne de la table dont la colonne colNumberSubString contient la chaine subStringInCell.
     * @param subStringInCell chaine pour identifier la ligne
     * @param colNumberSubString colonne de la ligne qui doit contenir subStringInCell
     * @param colNumberCellToRead colonne de la cellule dont on veut la valeur
     * @param attr attribut dont on veut la valeur
     * @return la valeur de l'attribut si Reporter.PASS_STATUS, sinon "row not found" ou COL_NOT_FOUND
     */
    public String getCellAttribute(String subStringInCell, int colNumberSubString, int colNumberCellToRead, String attr) {
        String value;
        try {
            value = getCell(subStringInCell, colNumberSubString, colNumberCellToRead).getAttribute(attr);
        } catch (Exception e) {
            value = e.getMessage();
        }
        return value;
    }


    /////////////////// ACTIONS
    /**
     * clique dans la cellule headerName de la première ligne contenant la valeur subStringInRow (la recherche de la chaine n'étant pas limitée à la colonne headerName).
     * Le résultat est tracé dans le rapport.
     * @param subStringInRow valeur que l'on cherche dans une ligne de la table
     * @param headerName nom de la colonne dans laquelle on va cliquer
     */
    public void clickCell(String subStringInRow, String headerName) {
        clickCell(getRowNumber(subStringInRow), getColNumber(headerName));
    }

    /**
     * clique dans la cellule colNumber de la première ligne contenant la valeur subStringInRow (la recherche de la chaine n'étant pas limitée à la colonne headerName).
     * Le résultat est tracé dans le rapport.
     * @param subStringInRow valeur que l'on cherche dans une ligne de la table
     * @param colNumber colonne dans laquelle on va cliquer
     */
    public void clickCell(String subStringInRow, int colNumber) {
        clickCell(getRowNumber(subStringInRow), colNumber);
    }

    /**
     * clique dans la cellule colNumber de la première ligne contenant la valeur subStringInRow (la recherche de la chaine n'étant pas limitée à la colonne headerName).
     * Le résultat est tracé dans le rapport.
     * @param rowNumber numero de la ligne, commence à 1
     * @param colNumber nom de la colonne dans laquelle on va cliquer
     */
    public void clickCell(int rowNumber, int colNumber) {
        log.info("Table.clickCell ");
        String result = Reporter.ERROR_STATUS;
        String errorMessage;
        BaseElement cell = getCell(rowNumber, colNumber);
        try {
            if (colNumber>0) {
                cell.click();
                errorMessage = null;
                result = Reporter.PASS_STATUS;
            } else {
                errorMessage = COL_NOT_FOUND;
            }
        } catch (Exception e) {
            errorMessage = e.getMessage();
        }
        this.getTestContext().getReport().log(result, "clickCell col " + colNumber + " ligne " + rowNumber, null, null , null, errorMessage);
    }
    /**
     * clique dans la cellule headerName de la première ligne rowNumber
     * Le résultat est tracé dans le rapport.
     * @param rowNumber numero de la ligne, commence à 1
     * @param headerName nom de la colonne dans laquelle on va cliquer
     */
    public void clickCell(int rowNumber, String headerName) {
        clickCell(rowNumber, headerName);
    }


    /**
     * Réalise une action sur une ligne de la table contenant une chaine de données subStringInRow. L'action est identifiée par tout ou partie de la valeur d'un attribut de son élément html.
     * Le résultat est tracé dans le rapport.
     * @param correspondingRow numero de la ligne
     * @param action tout ou partie de la valeur d'un attribut de l'élément de l'action (par exemple "common-pencil", "editer")
     */
    public void actionOnRow(int correspondingRow, String action) {
        log.info("Table.actionOnRow ");
        String result = Reporter.ERROR_STATUS;
        String errorMessage = "ligne " + correspondingRow + NON_TROUVEE;
        BaseElement actionButton = actionElementInRowByNum.injectValues(Map.of(ROW_NUMBER, String.valueOf(correspondingRow), ATTR_ACTION, action));
        try {
            if (correspondingRow>0) {
                actionButton.click();
                result = Reporter.PASS_STATUS;
                errorMessage = null;
            }
        } catch (Exception e) {
            errorMessage = e.getMessage();
            log.info(errorMessage);
        }
        this.getTestContext().getReport().log(result, "actionOnRow " + action + COL + correspondingRow, actionButton, null , null, errorMessage);
    }

    /**
     * Réalise une action sur une ligne de la table contenant une chaine de données subStringInRow. L'action est identifiée par tout ou partie de la valeur d'un attribut de son élément html.
     * Le résultat est tracé dans le rapport.
     * @param subStringInRow chaine que la ligne doit contenir
     * @param action tout ou partie de la valeur d'un attribut de l'élément de l'action (par exemple "common-pencil", "editer")
     */
    public void actionOnRow(String subStringInRow, String action) {
        actionOnRow(getRowNumber(subStringInRow), action);
    }


    /**
     * Réalise une action sur une ligne de la table contenant des chaines de données subStringsInRow. L'action est identifiée par tout ou partie de la valeur d'un attribut de son élément html.
     * Le résultat est tracé dans le rapport.
     * @param subStringsInRow chaine que la ligne doit contenir
     * @param action tout ou partie de la valeur d'un attribut de l'élément de l'action (par exemple "common-pencil", "editer")

     */
    public void actionOnRow(String[] subStringsInRow, String action) {
        BaseElement row = getRow(subStringsInRow);
        row.setContainer(this);
        BaseElement actionButton = actionElementInRow.injectValues(ATTR_ACTION, action);
        actionButton.setContainer(row);
        actionButton.click();
    }


    /**
     * Réalise une action sur une ligne de la table contenant une chaine de données subStringInCell dans la colonne colNumberSubString. L'action est identifiée par tout ou partie de la valeur d'un attribut de son élément html.
     * Le résultat est tracé dans le rapport.
     * @param subStringInCell valeur que l'on cherche dans la colonne subStringHeaderName
     * @param colNumberSubString numéro de la colonne dans laquelle on cherche subStringInCell
     * @param action tout ou partie de la valeur d'un attribut de l'élément de l'action (par exemple "common-pencil", "editer")
     */
    public void actionOnRow(String subStringInCell, int colNumberSubString, String action) {
        log.info("Table.actionOnRow ");
        String result = Reporter.ERROR_STATUS;
        String errorMessage = "ligne " + subStringInCell + NON_TROUVEE;
        BaseElement actionButton = null;
        try {
            int correspondingRow = getRowNumber(subStringInCell, colNumberSubString);
            if (correspondingRow>0) {
                actionButton = actionElementInRowByNum.injectValues(Map.of(ROW_NUMBER, String.valueOf(correspondingRow), ATTR_ACTION, action));
                actionButton.click();
                result = Reporter.PASS_STATUS;
                errorMessage = null;
            }
        } catch (Exception e) {
            errorMessage = e.getMessage();
            log.info(errorMessage);
        }
        this.getTestContext().getReport().log(result, "actionOnRow " + action + " sur ligne contenant " + subStringInCell, actionButton, null , null, errorMessage);
    }

    /**
     * Réalise une action sur une ligne de la table contenant une chaine de données subStringInCell dans la colonne subStringHeaderName. L'action est identifiée par tout ou partie de la valeur d'un attribut de son élément html.
     * Le résultat est tracé dans le rapport.
     * @param subStringInCell valeur que l'on cherche dans la colonne subStringHeaderName
     * @param subStringHeaderName nom de la colonne dans laquelle on cherche subStringInCell
     * @param action tout ou partie de la valeur d'un attribut de l'élément de l'action (par exemple "common-pencil", "editer")
     */
    public void actionOnRow(String subStringInCell, String subStringHeaderName, String action) {
        actionOnRow(subStringInCell, subStringHeaderName, action);
    }

    //////////////////////////////// ASSERTIONS
    /**
     * vérifie qu'une ligne de la table contient une valeur dans une colonne.
     * Le résultat est tracé dans le rapport.
     * @param subStringInRow valeur que l'on cherche dans la colonne subStringHeaderName
     */
    public void assertContains(String subStringInRow) {
        String action = "assertContains";
        boolean contains = this.contains(subStringInRow);
        startTry(action);
        int timeout = GlobalProp.getAssertTimeOut();
        while (!contains && ! stopTry(timeout,action)) {
            contains = this.contains(subStringInRow);
            timeout = TIME_OUT_ASSERTION;
        }
        this.getTestContext().getReport().log((contains?Reporter.PASS_STATUS:Reporter.FAIL_NEXT_STATUS), action + " " + subStringInRow , null, null , null, null);
    }

    /**
     * vérifie qu'aucune ligne de la table ne contient une valeur dans une colonne.
     * Le résultat est tracé dans le rapport.
     * @param subStringInRow valeur que l'on cherche dans la colonne subStringHeaderName
     */
    public void assertNotContains(String subStringInRow) {
        String action = "assertNotContains";
        boolean contains = this.contains(subStringInRow);
        startTry(action);
        int timeout = GlobalProp.getAssertTimeOut();
        while (contains && ! stopTry(timeout,action)) {
            contains = this.contains(subStringInRow);
            timeout = TIME_OUT_ASSERTION;
        }
        this.getTestContext().getReport().log((!contains?Reporter.PASS_STATUS:Reporter.FAIL_NEXT_STATUS), action + " " + subStringInRow , null, null , null, null);
    }

    /**
     * vérifie que les cellules d'une ligne de la table contiennent les valeurs passées en paramètre
     * @param values
     */
    public void assertOneRowContains(String[] values) {
        assertOneRowContains(values, true);
    }

    /**
     * vérifie que les cellules d'une ligne de la table ne contiennent pas les valeurs passées en paramètre
     * @param values
     */
    public void assertOneRowNotContains(String[] values) {
        assertOneRowContains(values, false);
    }

    /**
     * vérifie que les cellules d'une ligne de la table contiennent les valeurs passées en paramètre
     * @param values
     * @param contains
     */
    public void assertOneRowContains(String[] values, boolean contains) {
        String action = "assertOneRowContains " + contains;
        log.info(action);
        String status = Reporter.FAIL_NEXT_STATUS;
        BaseElement row = getRow(values);
        boolean exists = row.exists();
        startTry(action);
        int timeout = GlobalProp.getAssertTimeOut();
        while (exists!=contains && ! stopTry(timeout,action)) {
            exists = row.exists();
            timeout = TIME_OUT_ASSERTION;
        }
        if (exists==contains) {
            status = Reporter.PASS_STATUS;
        }
        this.getTestContext().getReport().log(status, action, row, null, null, null);
    }

    /**
     * vérifie que le contenu de la table en ligne rowNumber vaut rowValue
     * @param rowNumber
     * @param rowValue
     */
    public void assertRowValueEquals(int rowNumber, String rowValue) {
        String action = "assertRowValueEquals";
        log.info(action);
        String status = Reporter.PASS_STATUS;
        String value = getRowValue(rowNumber);
        startTry(action);
        int timeout = GlobalProp.getAssertTimeOut();
        while (!value.equals(DataUtil.normalizeSpace(rowValue)) && ! stopTry(timeout,action)) {
            value = getRowValue(rowNumber);
            timeout = TIME_OUT_ASSERTION;
        }
        if (!value.equals(DataUtil.normalizeSpace(rowValue))) {
            status = Reporter.FAIL_NEXT_STATUS;
        }
        this.getTestContext().getReport().log(status, action + " sur ligne " + rowNumber, null, rowValue, value, null);
    }

    /**
     * vérifie que la ligne rownum contient l'élément common.page.element.
     * Le résultat est tracé dans le rapport.
     * @param substringInRow chaine pour identifier la ligne
     * @param element common.page.element attendu dans la cellule
     */
    public void assertRowContainsElement(String substringInRow, BaseElement element) {
        assertRowContainsElement(getRowNumber(substringInRow), element);
    }

    /**
     * vérifie que la ligne rownum contient l'élément common.page.element.
     * Le résultat est tracé dans le rapport.
     * @param rownum chaine pour identifier la ligne
     * @param element common.page.element attendu dans la cellule
     */
    public void assertRowContainsElement(int rownum, BaseElement element) {
        log.info("Table.assertRowContainsElement ");
        String status = Reporter.FAIL_NEXT_STATUS;
        BaseElement row = null;
        try {
            row = oneRowByNum.injectValues(ROW_NUMBER, String.valueOf(rownum));
            row.setContainer(this);
            element.setContainer(row);
            if (element.exists()) {
                status = Reporter.PASS_STATUS;
            }
        } catch (Exception e) {
            // on fait rien
        }
        this.getTestContext().getReport().log(status, "assertRowContainsElement ", row, element.getName(), "", null);
    }

    /**
     * vérifie que la ligne rownum ne contient pas l'élément common.page.element.
     * Le résultat est tracé dans le rapport.
     * @param substringInRow chaine pour identifier la ligne
     * @param element common.page.element attendu dans la cellule
     */
    public void assertRowNotContainsElement(String substringInRow, BaseElement element) {
        assertRowNotContainsElement(getRowNumber(substringInRow), element);
    }

    /**
     * vérifie que la ligne rownum ne contient pas l'élément common.page.element.
     * Le résultat est tracé dans le rapport.
     * @param rownum chaine pour identifier la ligne
     * @param element common.page.element attendu dans la cellule
     */
    public void assertRowNotContainsElement(int rownum, BaseElement element) {
        log.info("Table.assertRowNotContainsElement ");
        String status = Reporter.FAIL_NEXT_STATUS;
        BaseElement row = oneRowByNum.injectValues(ROW_NUMBER, String.valueOf(rownum));
        row.setContainer(this);
        try {
            element.setContainer(row);
            if (!element.exists()) {
                status = Reporter.PASS_STATUS;
            }
        } catch (Exception e) {
            // on fait rien
        }
        this.getTestContext().getReport().log(status, "assertRowNotContainsElement ", row, element.getName(), "", null);
    }

    /**
     * vérifie que la cellule de la colonne cellToReadHeaderName de la ligne de la table dont la colonne subStringHeaderName contient la chaine subStringInCell contient l'élément common.page.element.
     * Le résultat est tracé dans le rapport.
     * @param subStringInCell chaine pour identifier la ligne
     * @param subStringHeaderName colonne de la ligne qui doit contenir subStringInCell
     * @param cellToReadHeaderName colonne de la cellule dont on veut la valeur
     * @param element common.page.element attendu dans la cellule
     */
    public void assertCellContainsElement(String subStringInCell, String subStringHeaderName, String cellToReadHeaderName, BaseElement element) {
        assertCellContainsElement( subStringInCell,  getColNumber(subStringHeaderName),  getColNumber(cellToReadHeaderName),  element);
    }

    /**
     * vérifie que la cellule de la colonne colNumberSubString de la ligne colNumberCellToRead contient l'élément common.page.element.
     * Le résultat est tracé dans le rapport.
     * @param subStringInCell chaine pour identifier la ligne
     * @param colNumberSubString colonne de la ligne qui doit contenir subStringInCell
     * @param colNumberCellToRead colonne de la cellule dont on veut la valeur
     * @param element common.page.element attendu dans la cellule
     */
    public void assertCellContainsElement(String subStringInCell, int colNumberSubString, int colNumberCellToRead, BaseElement element) {
        assertCellContainsElement(getRowNumber(subStringInCell, colNumberSubString), colNumberCellToRead, element);
    }

    /**
     * vérifie que la cellule de la colonne cellToReadHeaderName de la ligne numero row contient la chaine subStringInCell contient l'élément common.page.element.
     * Le résultat est tracé dans le rapport.
     * @param row numero de la ligne
     * @param cellToReadHeaderName colonne de la cellule dont on veut la valeur
     * @param element common.page.element attendu dans la cellule
     */
    public void assertCellContainsElement(int row, String cellToReadHeaderName, BaseElement element) {
        assertCellContainsElement(row, getColNumber(cellToReadHeaderName),  element);
    }

    /**
     * vérifie que la cellule de la colonne colNumberSubString de la ligne row contient l'élément common.page.element.
     * Le résultat est tracé dans le rapport.
     * @param row numero de la ligne
     * @param colNumberCellToRead colonne de la cellule dont on veut la valeur
     * @param element common.page.element attendu dans la cellule
     */
    public void assertCellContainsElement(int row, int colNumberCellToRead, BaseElement element) {
        log.info("Table.assertCellContainsElement ");
        String status = Reporter.FAIL_NEXT_STATUS;
        BaseElement cell = null;
        try {
            cell = getCell(row, colNumberCellToRead);
            element.setContainer(cell);
            if (element.exists()) {
                status = Reporter.PASS_STATUS;
            }
        } catch (Exception e) {
            // on fait rien
        }
        this.getTestContext().getReport().log(status, "assertCellContainsElement ", cell, element.getName(), "", null);
    }


    /**
     * vérifie que la cellule de la colonne cellToReadHeaderName de la ligne de la table dont la colonne subStringHeaderName contient la chaine subStringInCell ne contient pas l'élément common.page.element.
     * Le résultat est tracé dans le rapport.
     * @param subStringInCell chaine pour identifier la ligne
     * @param subStringHeaderName colonne de la ligne qui doit contenir subStringInCell
     * @param cellToReadHeaderName colonne de la cellule dont on veut la valeur
     * @param element common.page.element attendu dans la cellule
     */
    public void assertCellNotContainsElement(String subStringInCell, String subStringHeaderName, String cellToReadHeaderName, BaseElement element) {
        assertCellNotContainsElement( subStringInCell,  getColNumber(subStringHeaderName),  getColNumber(cellToReadHeaderName),  element);
    }

    /**
     * vérifie que la cellule de la colonne colNumberSubString de la ligne colNumberCellToRead ne contient pas l'élément common.page.element.
     * Le résultat est tracé dans le rapport.
     * @param subStringInCell chaine pour identifier la ligne
     * @param colNumberSubString colonne de la ligne qui doit contenir subStringInCell
     * @param colNumberCellToRead colonne de la cellule dont on veut la valeur
     * @param element common.page.element attendu dans la cellule
     */
    public void assertCellNotContainsElement(String subStringInCell, int colNumberSubString, int colNumberCellToRead, BaseElement element) {
        assertCellNotContainsElement(getRowNumber(subStringInCell, colNumberSubString), colNumberCellToRead, element);
    }

    /**
     * vérifie que la cellule de la colonne cellToReadHeaderName de la ligne row contient la chaine subStringInCell ne contient pas l'élément common.page.element.
     * Le résultat est tracé dans le rapport.
     * @param row numero de la ligne
     * @param cellToReadHeaderName colonne de la cellule dont on veut la valeur
     * @param element common.page.element attendu dans la cellule
     */
    public void assertCellNotContainsElement(int row, String cellToReadHeaderName, BaseElement element) {
        assertCellNotContainsElement(row, getColNumber(cellToReadHeaderName),  element);
    }


    /**
     * vérifie que la cellule de la colonne colNumberSubString de la ligne row ne contient pas l'élément common.page.element.
     * Le résultat est tracé dans le rapport.
     * @param row numero de la ligne
     * @param colNumberCellToRead colonne de la cellule dont on veut la valeur
     * @param element common.page.element attendu dans la cellule
     */
    public void assertCellNotContainsElement(int row, int colNumberCellToRead, BaseElement element) {
        log.info("Table.assertCellNotContainsElement ");
        String status = Reporter.FAIL_NEXT_STATUS;
        BaseElement cell = null;
        try {
            cell = getCell(row, colNumberCellToRead);
            element.setContainer(cell);
            if (!element.exists()) {
                status = Reporter.PASS_STATUS;
            }
        } catch (Exception e) {
            // on fait rien
        }
        this.getTestContext().getReport().log(status, "assertCellNotContainsElement ", cell, element.getName(), "", null);
    }

    /**
     * vérifie que la valeur de la colonne headerName de la ligne contenant subStringInRow vaut cellValue.
     * Le résultat est tracé dans le rapport.
     * @param subStringInRow chaine pour identifier la ligne
     * @param headerName colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     */
    public void assertCellValueEquals(String subStringInRow, String headerName, String cellValue) {
        assertCellValueEquals(subStringInRow, getColNumber(headerName), cellValue);
    }

    /**
     * vérifie que la valeur de la colonne headerName de la ligne contenant subStringInRow vaut cellValue.
     * Le résultat est tracé dans le rapport.
     * @param rowNumber numero la ligne
     * @param headerName colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     */
    public void assertCellValueEquals(int rowNumber, String headerName, String cellValue) {
        assertCellValueEquals(rowNumber, getColNumber(headerName), cellValue);
    }

    /**
     * vérifie que la valeur de la colonne colNumber de la ligne contenant subStringInRow vaut cellValue.
     * Le résultat est tracé dans le rapport.
     * @param subStringInRow chaine pour identifier la ligne
     * @param colNumber colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     */
    public void assertCellValueEquals(String subStringInRow, int colNumber, String cellValue) {
        assertCellValueEquals(getRowNumber(subStringInRow), colNumber, cellValue);
    }

    /**
     * vérifie que la valeur de la colonne colNumber de la ligne contenant subStringInRow vaut cellValue.
     * Le résultat est tracé dans le rapport.
     * @param rowNumber numero de la ligne
     * @param colNumber colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     */
    public void assertCellValueEquals(int rowNumber, int colNumber, String cellValue) {
        getCell(rowNumber, colNumber).assertValue(cellValue);
    }

    /**
     * vérifie que valeur de cellule de la colonne cellToReadHeaderName de la ligne de la table dont la colonne subStringHeaderName contient la chaine subStringInCell vaut cellValue.
     * Le résultat est tracé dans le rapport.
     * @param subStringInCell chaine pour identifier la ligne
     * @param subStringHeaderName colonne de la ligne qui doit contenir subStringInCell
     * @param cellToReadHeaderName colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     */
    public void assertCellValueEquals(String subStringInCell, String subStringHeaderName, String cellToReadHeaderName, String cellValue) {
        assertCellValueEquals(subStringInCell, getColNumber(subStringHeaderName), getColNumber(cellToReadHeaderName), cellValue);
    }
    /**
     * vérifie que valeur de cellule de la colonne colNumberCellToRead de la ligne de la table dont la colonne colNumberSubString contient la chaine subStringInCell vaut cellValue.
     * Le résultat est tracé dans le rapport.
     * @param subStringInCell chaine pour identifier la ligne
     * @param colNumberSubString colonne de la ligne qui doit contenir subStringInCell
     * @param colNumberCellToRead colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     */
    public void assertCellValueEquals(String subStringInCell, int colNumberSubString, int colNumberCellToRead, String cellValue) {
        getCell(subStringInCell, colNumberSubString, colNumberCellToRead).assertValue(cellValue);
    }


    //---------------------------------------------------------------------------------
    /**
     * renvoi la valeur d'une cellule en double
     * @param subStringInRow
     * @param colNumber
     * @return
     */
    public double getCellValueAsDouble(String subStringInRow, int colNumber) {
        Loader.waitNotVisible();
        String value = getCellValue(subStringInRow, colNumber);
        try {
            String readVal = DoubleUtil.asNum(value);
            return Double.parseDouble(readVal.equals("")?"0":readVal);
        } catch (Exception e) {
            this.getTestContext().getReport().log(Reporter.ERROR_STATUS, value +  " n'est pas numérique");
            return -1;
        }
    }
    /**
     * renvoi la valeur d'une cellule en double
     * @param subStringsInRow
     * @param colNumber
     * @return
     */
    public double getCellValueAsDouble(String[] subStringsInRow, int colNumber) {
        Loader.waitNotVisible();
        String value = getCellValue(subStringsInRow, colNumber);
        try {
            String readVal = DoubleUtil.asNum(value);
            return Double.parseDouble(readVal.equals("")?"0":readVal);
        } catch (Exception e) {
            this.getTestContext().getReport().log(Reporter.ERROR_STATUS, value +  " n'est pas numérique");
            return -1;
        }
    }


    //---------------------------------------------------------------------------------
    /**
     * vérifie que la valeur de la colonne colNumber de la ligne contenant subStringInRow vaut cellValue en tant que double.
     * Le résultat est tracé dans le rapport.
     * @param subStringInRow chaine pour identifier la ligne
     * @param colHeader colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     */
    public void assertCellValueAsDoubleEquals(String subStringInRow, String colHeader, String cellValue) {
        assertCellValueAsDoubleEquals(subStringInRow, getColNumber(colHeader), cellValue);
    }

    /**
     * vérifie que la valeur de la colonne colNumber de la ligne contenant subStringInRow vaut cellValue en tant que double.
     * Le résultat est tracé dans le rapport.
     * @param subStringInRow chaine pour identifier la ligne
     * @param colNumber colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     */
    public void assertCellValueAsDoubleEquals(String subStringInRow, int colNumber, String cellValue) {
        String status = Reporter.PASS_STATUS;
        startTry("assertCellValueAsDoubleEquals");
        Double expectedValue = Double.parseDouble(DoubleUtil.asNum(cellValue));
        Double value = getCellValueAsDouble(subStringInRow, colNumber);
        while (Math.abs(expectedValue-value)>=0.01 && ! stopTry(30,"assertCellValueAsDoubleEquals")) {
            value = getCellValueAsDouble(subStringInRow, colNumber);
        }
        if (Math.abs(expectedValue-value)>=0.01) {
            status = Reporter.FAIL_NEXT_STATUS;
        }
        this.getTestContext().getReport().log(status, "assertCellValueAsDoubleEquals colonne " + colNumber + CONTENANT + subStringInRow, null, cellValue, String.valueOf(value), null);
    }

    /**
     * vérifie que la valeur de la colonne colNumber de la ligne contenant subStringsInRow vaut cellValue en tant que double.
     * Le résultat est tracé dans le rapport.
     * @param subStringsInRow chaine pour identifier la ligne
     * @param colHeader colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     */
    public void assertCellValueAsDoubleEquals(String[] subStringsInRow, String colHeader, String cellValue) {
        assertCellValueAsDoubleEquals(subStringsInRow, getColNumber(colHeader), cellValue);
    }

    /**
     * vérifie que la valeur de la colonne colNumber de la ligne contenant subStringsInRow vaut cellValue en tant que double.
     * Le résultat est tracé dans le rapport.
     * @param subStringsInRow chaine pour identifier la ligne
     * @param colNumber colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     */
    public void assertCellValueAsDoubleEquals(String[] subStringsInRow, int colNumber, String cellValue) {
        String status = Reporter.PASS_STATUS;
        startTry("assertCellValueEquals");
        Double expectedValue = Double.parseDouble(DoubleUtil.asNum(cellValue));
        Double value = getCellValueAsDouble(subStringsInRow, colNumber);
        while (Math.abs(expectedValue-value)>=0.01 && ! stopTry(30,"assertCellValueEquals")) {
            value = getCellValueAsDouble(subStringsInRow, colNumber);
        }
        if (Math.abs(expectedValue-value)>=0.01) {
            status = Reporter.FAIL_NEXT_STATUS;
        }
        this.getTestContext().getReport().log(status, "assertCellValueEquals colonne " + colNumber + CONTENANT + Arrays.toString(subStringsInRow), null, cellValue, String.valueOf(value), null);
    }

    /**
     * vérifie que la valeur de la colonne headerName de la ligne contenant subStringInRow contient cellValue.
     * Le résultat est tracé dans le rapport.
     * @param rowNumber numero la ligne
     * @param headerName colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     */
    public void assertCellValueContains(int rowNumber, String headerName, String cellValue) {
        assertCellValueContains(rowNumber, getColNumber(headerName), cellValue);
    }
    /**
     * vérifie que la valeur de la colonne headerName de la ligne contenant subStringInRow contient cellValue.
     * Le résultat est tracé dans le rapport.
     * @param subStringInRow chaine pour identifier la ligne
     * @param headerName colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     */
    public void assertCellValueContains(String subStringInRow, String headerName, String cellValue) {
        assertCellValueContains(subStringInRow, getColNumber(headerName), cellValue);
    }
    /**
     * vérifie que la valeur de la colonne colNumber de la ligne contenant subStringInRow contient cellValue.
     * Le résultat est tracé dans le rapport.
     * @param subStringInRow chaine pour identifier la ligne
     * @param colNumber colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     */
    public void assertCellValueContains(String subStringInRow, int colNumber, String cellValue) {
        assertCellValueContains(getRowNumber(subStringInRow), colNumber, cellValue);
    }
    /**
     * vérifie que la valeur de la colonne colNumber de la ligne contenant subStringInRow contient cellValue.
     * Le résultat est tracé dans le rapport.
     * @param rowNumber numero la ligne
     * @param colNumber colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     */
    public void assertCellValueContains(int rowNumber, int colNumber, String cellValue) {
        getCell(rowNumber, colNumber).assertValueContains(cellValue);
    }

    /**
     * vérifie que valeur de cellule de la colonne cellToReadHeaderName de la ligne de la table dont la colonne subStringHeaderName contient la chaine subStringInCell contient cellValue.
     * Le résultat est tracé dans le rapport.
     * @param subStringInCell chaine pour identifier la ligne
     * @param subStringHeaderName colonne de la ligne qui doit contenir subStringInCell
     * @param cellToReadHeaderName colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     */
    public void assertCellValueContains(String subStringInCell, String subStringHeaderName, String cellToReadHeaderName, String cellValue) {
        assertCellValueContains(subStringInCell, getColNumber(subStringHeaderName), getColNumber(cellToReadHeaderName), cellValue);
    }
    /**
     * vérifie que valeur de cellule de la colonne colNumberCellToRead de la ligne de la table dont la colonne colNumberSubString contient la chaine subStringInCell contient cellValue.
     * Le résultat est tracé dans le rapport.
     * @param subStringInCell chaine pour identifier la ligne
     * @param colNumberSubString colonne de la ligne qui doit contenir subStringInCell
     * @param colNumberCellToRead colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     */
    public void assertCellValueContains(String subStringInCell, int colNumberSubString, int colNumberCellToRead, String cellValue) {
        getCell(subStringInCell, colNumberSubString, colNumberCellToRead).assertValueContains(cellValue);
    }


    /**
     * vérifie que la valeur de la colonne headerName de la ligne contenant subStringInRow ne contient pas cellValue.
     * Le résultat est tracé dans le rapport.
     * @param subStringInRow chaine pour identifier la ligne
     * @param headerName colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     */
    public void assertCellValueNotContains(String subStringInRow, String headerName, String cellValue) {
        assertCellValueNotContains(subStringInRow, getColNumber(headerName), cellValue);
    }
    /**
     * vérifie que la valeur de la colonne colNumber de la ligne contenant subStringInRow ne contient pas cellValue.
     * Le résultat est tracé dans le rapport.
     * @param subStringInRow chaine pour identifier la ligne
     * @param colNumber colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     */
    public void assertCellValueNotContains(String subStringInRow, int colNumber, String cellValue) {
        assertCellValueNotContains(getRowNumber(subStringInRow), colNumber, cellValue);
    }
    /**
     * vérifie que la valeur de la colonne colNumber de la ligne contenant subStringInRow ne contient pas cellValue.
     * Le résultat est tracé dans le rapport.
     * @param rowNumber numero la ligne
     * @param colNumber colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     */
    public void assertCellValueNotContains(int rowNumber, int colNumber, String cellValue) {
        getCell(rowNumber, colNumber). assertValueDoesntContain(cellValue);
    }

    /**
     * vérifie qu'une ligne de la table contient une valeur dans une colonne.
     * Le résultat est tracé dans le rapport.
     * @param subStringInCell valeur que l'on cherche dans la colonne subStringHeaderName
     * @param subStringHeaderName nom de la colonne dans laquelle on cherche subStringInCell
     */
    public void assertContains(String subStringInCell, String subStringHeaderName) {
        assertContains(subStringInCell, getColNumber(subStringHeaderName));
    }
    /**
     * vérifie qu'une ligne de la table contient une valeur dans la colonne colNumberSubString.
     * Le résultat est tracé dans le rapport.
     * @param subStringInCell valeur que l'on cherche dans la colonne subStringHeaderName
     * @param colNumberSubString numéro de la colonne dans laquelle on cherche subStringInCell
     */
    public void assertContains(String subStringInCell, int colNumberSubString) {
        String action = "assertContains";
        boolean contains = columnContains(subStringInCell, colNumberSubString);
        startTry(action);
        int timeout = GlobalProp.getAssertTimeOut();
        while (!contains && ! stopTry(timeout,action)) {
            contains = columnContains(subStringInCell, colNumberSubString);
            timeout = TIME_OUT_ASSERTION;
        }
        this.getTestContext().getReport().log((contains?Reporter.PASS_STATUS:Reporter.FAIL_NEXT_STATUS), action + " " + subStringInCell + COL + colNumberSubString, null, null , null, null);
    }

    /**
     * vérifie qu'aucune ligne de la table ne contient une valeur dans une colonne.
     * Le résultat est tracé dans le rapport.
     * @param subStringInCell valeur que l'on cherche dans la colonne subStringHeaderName
     * @param subStringHeaderName nom de la colonne dans laquelle on cherche subStringInCell
     */
    public void assertNotContains(String subStringInCell, String subStringHeaderName) {
        assertNotContains(subStringInCell, getColNumber(subStringHeaderName));
    }
    /**
     * vérifie qu'aucune ligne de la table ne contient une valeur dans une colonne.
     * Le résultat est tracé dans le rapport.
     * @param subStringInCell valeur que l'on cherche dans la colonne subStringHeaderName
     * @param colNumberSubString numéro de la colonne dans laquelle on cherche subStringInCell
     */
    public void assertNotContains(String subStringInCell, int colNumberSubString) {
        String action = "assertNotContains";
        boolean contains = columnContains(subStringInCell, colNumberSubString);
        startTry(action);
        int timeout = GlobalProp.getAssertTimeOut();
        while (contains && ! stopTry(timeout,action)) {
            contains = columnContains(subStringInCell, colNumberSubString);
            timeout = TIME_OUT_ASSERTION;
        }
        this.getTestContext().getReport().log((!contains?Reporter.PASS_STATUS:Reporter.FAIL_NEXT_STATUS), action + " " + subStringInCell + COL + colNumberSubString, null, null , null, null);
    }

    /**
     * vérifie que la valeur de l'attribut attr d'une cellule de la colonne cellToReadHeaderName de la ligne de la table dont la colonne subStringHeaderName contient la chaine subStringInCell.
     * Le résultat est tracé dans le rapport.
     * @param subStringInCell chaine pour identifier la ligne
     * @param subStringHeaderName colonne de la ligne qui doit contenir subStringInCell
     * @param cellToReadHeaderName colonne de la cellule dont on veut la valeur
     * @param attr attribut dont on veut la valeur
     * @param attributeValue tout ou partie de la valeur attendu de l'attribut
     */
    public void assertCellAttributeContains(String subStringInCell, String subStringHeaderName, String cellToReadHeaderName, String attr, String attributeValue) {
        assertCellAttributeContains(subStringInCell, getColNumber(subStringHeaderName), getColNumber(cellToReadHeaderName), attr, attributeValue);
    }
    /**
     * vérifie que la valeur de l'attribut attr d'une cellule de la colonne colNumberCellToRead de la ligne de la table dont la colonne colNumberSubString contient la chaine subStringInCell.
     * Le résultat est tracé dans le rapport.
     * @param subStringInCell chaine pour identifier la ligne
     * @param colNumberSubString colonne de la ligne qui doit contenir subStringInCell
     * @param colNumberCellToRead colonne de la cellule dont on veut la valeur
     * @param attr attribut dont on veut la valeur
     * @param attributeValue tout ou partie de la valeur attendu de l'attribut
     */
    public void assertCellAttributeContains(String subStringInCell, int colNumberSubString, int colNumberCellToRead, String attr, String attributeValue) {
        String action = "assertCellAttributeContains";
        log.info(action);
        String status = Reporter.PASS_STATUS;
        String value = getCellAttribute(subStringInCell, colNumberSubString, colNumberCellToRead, attr);
        startTry(action);
        int timeout = GlobalProp.getAssertTimeOut();
        while (!value.contains(attributeValue) && ! stopTry(timeout,action)) {
            value = getCellAttribute(subStringInCell, colNumberSubString, colNumberCellToRead, attr);
            timeout = TIME_OUT_ASSERTION;
        }
        if (!value.contains(attributeValue)) {
            status = Reporter.FAIL_NEXT_STATUS;
        }
        this.getTestContext().getReport().log(status, action + SUR_COL + colNumberCellToRead + CONTENANT + subStringInCell, null,  attributeValue, value, null);
    }

    /**
     * vérifie que la valeur de l'attribut attr d'une cellule de la colonne cellToReadHeaderName de la ligne de la table dont la colonne subStringHeaderName contient la chaine subStringInCell.
     * Le résultat est tracé dans le rapport.
     * @param subStringInCell chaine pour identifier la ligne
     * @param subStringHeaderName colonne de la ligne qui doit contenir subStringInCell
     * @param cellToReadHeaderName colonne de la cellule dont on veut la valeur
     * @param attr attribut dont on veut la valeur
     * @param attributeValue sous-chaine non attendue dans la valeur de l'attribut
     */
    public void assertCellAttributeNotContains(String subStringInCell, String subStringHeaderName, String cellToReadHeaderName, String attr, String attributeValue) {
        assertCellAttributeNotContains(subStringInCell, getColNumber(subStringHeaderName), getColNumber(cellToReadHeaderName), attr, attributeValue);
    }
    /**
     * vérifie que la valeur de l'attribut attr d'une cellule de la colonne colNumberCellToRead de la ligne de la table dont la colonne colNumberSubString contient la chaine subStringInCell.
     * Le résultat est tracé dans le rapport.
     * @param subStringInCell chaine pour identifier la ligne
     * @param colNumberSubString colonne de la ligne qui doit contenir subStringInCell
     * @param colNumberCellToRead colonne de la cellule dont on veut la valeur
     * @param attr attribut dont on veut la valeur
     * @param attributeValue sous-chaine non attendue dans la valeur de l'attribut
     */
    public void assertCellAttributeNotContains(String subStringInCell, int colNumberSubString, int colNumberCellToRead, String attr, String attributeValue) {
        String action = "assertCellAttributeNotContains";
        log.info(action);
        String status = Reporter.PASS_STATUS;
        String value = getCellAttribute(subStringInCell,colNumberSubString, colNumberCellToRead, attr);
        startTry(action);
        int timeout = GlobalProp.getAssertTimeOut();
        while (value.contains(attributeValue) && ! stopTry(timeout,action)) {
            value = getCellAttribute(subStringInCell,colNumberSubString, colNumberCellToRead, attr);
            timeout = TIME_OUT_ASSERTION;
        }
        if (value.contains(attributeValue)) {
            status = Reporter.FAIL_NEXT_STATUS;
        }
        this.getTestContext().getReport().log(status, action + SUR_COL + colNumberCellToRead+ CONTENANT + subStringInCell, null,  attributeValue, value, null);
    }

    /**
     * vérifie que la valeur de la colonne headerName du footer vaut cellValue.
     * Le résultat est tracé dans le rapport.
     * @param headerName colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     */
    public void assertFooterCellValueEquals(String headerName, String cellValue) {
        assertFooterCellValueEquals(getColNumber(headerName), cellValue);
    }

    /**
     * vérifie que la valeur de la colonne colNumber ddu footer vaut cellValue.
     * Le résultat est tracé dans le rapport.
     * @param colNumber colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     */
    public void assertFooterCellValueEquals(int colNumber, String cellValue) {
        String action = "assertFooterCellValueEquals";
        log.info(action);
        String status = Reporter.PASS_STATUS;
        String value = getFooterCellValue(colNumber);
        startTry(action);
        int timeout = GlobalProp.getAssertTimeOut();
        while (!value.equals(DataUtil.normalizeSpace(cellValue)) && ! stopTry(timeout,action)) {
            value = getFooterCellValue(colNumber);
            timeout = TIME_OUT_ASSERTION;
        }
        if (!value.equals(DataUtil.normalizeSpace(cellValue))) {
            status = Reporter.FAIL_NEXT_STATUS;
        }
        this.getTestContext().getReport().log(status, action + COL + colNumber, null, cellValue, value, null);
    }



    /**
     * renvoi la valeur d'une cellule en double
     * @param colNumber
     * @return
     */
    public double getFooterCellValueAsDouble(int colNumber) {
        return Double.parseDouble(DoubleUtil.asNum(getFooterCellValue(colNumber)));
    }

    /**
     * vérifie que la valeur de la colonne colNumber de la ligne contenant subStringInRow vaut cellValue en tant que double.
     * Le résultat est tracé dans le rapport.
     * @param colHeader colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     */
    public void assertFooterCellValueAsDoubleEquals(String colHeader, String cellValue) {
        assertFooterCellValueAsDoubleEquals(getColNumber(colHeader), cellValue);
    }



    /**
     * vérifie que la valeur de la colonne colNumber de la ligne contenant subStringInRow vaut cellValue en tant que double.
     * Le résultat est tracé dans le rapport.
     * @param colNumber colonne de la cellule dont on veut la valeur
     * @param cellValue valeur attendue
     */
    public void assertFooterCellValueAsDoubleEquals(int colNumber, String cellValue) {
        String action = "assertFooterCellValueAsDoubleEquals";
        log.info(action);
        String status = Reporter.PASS_STATUS;
        Double expectedValue = Double.parseDouble(cellValue.trim().replace("€","").replace(" ","").replace("&nbsp;","").replace(",","."));
        Double value = getFooterCellValueAsDouble(colNumber);
        startTry(action);
        int timeout = GlobalProp.getAssertTimeOut();
        while (!expectedValue.equals(value) && ! stopTry(timeout,action)) {
            value = getFooterCellValueAsDouble(colNumber);
            timeout = TIME_OUT_ASSERTION;
        }
        if (!expectedValue.equals(value)) {
            status = Reporter.FAIL_NEXT_STATUS;
        }
        this.getTestContext().getReport().log(status, action + SUR_COL + colNumber, null, cellValue, String.valueOf(value), null);
    }





    /**
     * Verifie le nombre de ligne de la table.
     */
    public void assertRowCount(int nbRow) {
        int i = 0;
        int rowCount = getRowCount();
        while (i<10 && nbRow!=rowCount) {rowCount = getRowCount();i++;}
        getReport().assertEquals("vérification nombre de ligne dans la table ", nbRow, rowCount);
    }


    /**
     * contruit un bout de xpath pour trouver une ligne contenant un ensemble de valeur
     * @param cellsValues
     * @return
     */
    private String[] buildXpathFindRowOrCell(String[] cellsValues) {
        StringBuilder sCellsValues = new StringBuilder();
        StringBuilder builtValuesXpath = new StringBuilder("[td]");
        for(String cellValue: cellsValues) {
            if (cellValue!=null && !cellValue.equals("null")) {
                sCellsValues.append(cellValue).append(" ");
                builtValuesXpath.append("[descendant::*[@*=normalize-space(\"").append(cellValue).append("\") or contains(normalize-space(), normalize-space(\"").append(cellValue).append("\"))]]");
            }
        }
        return new String[] {sCellsValues.toString(), builtValuesXpath.toString()};
    }
}

