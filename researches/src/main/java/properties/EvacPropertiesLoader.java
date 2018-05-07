package properties;

import distribution.NormativeDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EvacPropertiesLoader {
    final Logger log = LoggerFactory.getLogger(EvacPropertiesLoader.class);

    private String pathToProperties;
    private String bimFile;
    private DistributionType distribution;
    private double d0;
    private double k;
    private NormativeDistributionType normativeType;
    private int seed;
    private String resultsFolder;
    private String cfastFile;
    private Double beginEvacuation;
    private boolean isFire;

    public EvacPropertiesLoader(String pathToProperties) {
        this.pathToProperties = pathToProperties;

        Properties prop = null;
        try (InputStream is = new FileInputStream(pathToProperties)) {
            prop = new Properties();
            prop.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (prop == null) return;
        log.info("Чтение файла настроек {}", pathToProperties);

        final String sBimFile = "bim.file";
        if (prop.getProperty(sBimFile) == null) {
            log.error("Неполадки с полем {}. Работа программы остановлена. Проверте конфигурационный файл {}", sBimFile, pathToProperties);
            System.exit(0);
        } else {
            bimFile = prop.getProperty(sBimFile);
            if (bimFile.isEmpty()) {
                log.error("Файл пространственно-информационной модели не найден (поле {}). Укажите имя файла с расширение json", sBimFile);
            } else {
                log.info("{} = {}", sBimFile, bimFile);
            }
        }

        final String sBimDistribution = "bim.distribution";
        if (prop.getProperty(sBimDistribution) == null) {
            log.error("Неполадки с полем {}. Работа программы остановлена. Проверте конфигурационный файл {}", sBimDistribution, pathToProperties);
            System.exit(0);
        } else {
            String bimDistribution = prop.getProperty(sBimDistribution);
            if (bimDistribution.isEmpty()) {
                log.error("Не указана информация о способе размещения людей в здании (поле {}). Доступны два " +
                        "варината: random или normative", sBimDistribution);
            } else if (!bimDistribution.equals("random") && !bimDistribution.equals("normative")) {
                log.error("Некорректно указан способ размещения людей в здании (поле {}). Доступны два " +
                        "варината: random или normative", sBimDistribution);
            } else {
                switch (bimDistribution) {
                    case "random":
                        distribution = DistributionType.RANDOM;
                        break;
                    case "normative":
                        distribution = DistributionType.NORMATIVE;
                        break;
                }
                log.info("{} = {}", sBimDistribution, distribution);
            }
        }

        if (distribution == DistributionType.RANDOM) {
            final String sDistributionD0 = "distribution.random.d0";
            if (prop.getProperty(sDistributionD0) == null) {
                log.error("Неполадки с полем {}. Работа программы остановлена. Проверте конфигурационный файл {}", sDistributionD0, pathToProperties);
                System.exit(0);
            } else {
                String randomDistributionD0 = prop.getProperty(sDistributionD0);
                if (randomDistributionD0.isEmpty()) {
                    log.error("Не указана информация о средней плотности размещения: {}", sDistributionD0);
                } else {
                    try {
                        d0 = Double.parseDouble(randomDistributionD0);
                    } catch (NumberFormatException e) {
                        log.error("Возникли проблемы при чтении значения поля {}. Проверте правильность " +
                                "написания значения\n", sDistributionD0, e);
                    }
                    log.info("{} = {}", sDistributionD0, d0);
                }
            }

            final String sDistributionK = "distribution.random.k";
            if (prop.getProperty(sDistributionK) == null) {
                log.error("Неполадки с полем {}. Работа программы остановлена. Проверте конфигурационный файл {}", sDistributionK, pathToProperties);
                System.exit(0);
            } else {
                String randomDistributionK = prop.getProperty(sDistributionK);
                if (randomDistributionK.isEmpty()) {
                    log.error("Не указана информация о коэффицинте различия плотности: {} \n" +
                            "Допустимо указывать только значения больше нуля.", sDistributionK);
                } else {
                    try {
                        k = Double.parseDouble(randomDistributionK);
                    } catch (NumberFormatException e) {
                        log.error("Возникли проблемы при чтении значения поля {}. Проверте правильность " +
                                "написания значения\n", sDistributionK, e);
                    }
                    log.info("{} = {}", sDistributionK, k);
                }
            }
        } else if (distribution == DistributionType.NORMATIVE) {
            final String sDistributionNormative = "distribution.normative";
            if (prop.getProperty(sDistributionNormative) == null) {
                log.error("Неполадки с полем {}. Работа программы остановлена. Проверте конфигурационный файл {}", sDistributionNormative, pathToProperties);
                System.exit(0);
            } else {
                String normativeDistributionType = prop.getProperty(sDistributionNormative);
                if (normativeDistributionType.isEmpty()) {
                    log.error("Не указана информация о типе нормативного распределения:  \n" +
                            "Допустимо: random или uniform.", sDistributionNormative);
                } else {
                    switch (normativeDistributionType) {
                        case "random":
                            normativeType = NormativeDistributionType.RANDOM;
                            break;
                        case "uniform":
                            normativeType = NormativeDistributionType.UNIFORM;
                            break;
                    }
                    log.info("{} = {}", sDistributionNormative, normativeType);
                }
            }
        }

        final String sDistributionSeed = "distribution.seed";
        if (prop.getProperty(sDistributionSeed) == null) {
            log.error("Неполадки с полем {}. Работа программы остановлена. Проверте конфигурационный файл {}", sDistributionSeed, pathToProperties);
            System.exit(0);
        } else {
            String distributionSeed = prop.getProperty(sDistributionSeed);
            if (distributionSeed.isEmpty()) {
                log.error("Не указана информация о начальном значении генератора случайных чисел: {} \n" +
                        "Допустимо указывать только целые числа больше нуля.", sDistributionSeed);
            } else {
                try {
                    seed = Integer.parseInt(distributionSeed);
                } catch (NumberFormatException e) {
                    log.error("Возникли проблемы при чтении значения поля {}. Проверте правильность " +
                            "написания значения\n", sDistributionSeed, e);
                }
                log.info("{} = {}", sDistributionSeed, seed);
            }
        }

        /*final String sResultsFolder = "results.folder";
        if (prop.getProperty(sResultsFolder) == null) {
            log.error("Неполадки с полем {}. Работа программы остановлена. Проверте конфигурационный файл {}", sResultsFolder, pathToProperties);
            System.exit(0);
        } else {
            String resFolder = prop.getProperty(sResultsFolder);
            if (resFolder.isEmpty()) {
                log.error("Не указана папка для сохранения результатов");
            } else {
                resultsFolder = new File(resFolder);
                log.info("{} = {}", sResultsFolder, resultsFolder);
            }
        }*/

        resultsFolder = getPropValueStr("results.folder", prop);
        cfastFile = getPropValueStr("bim.fire.file", prop);
        isFire = Boolean.parseBoolean(getPropValueStr("bim.fire.is", prop));
        beginEvacuation = Double.parseDouble(getPropValueStr("bim.evac.start", prop));
    }

    private String getPropValueStr(String pPropName, Properties prop) {
        String nPropValue = prop.getProperty(pPropName);
        if (nPropValue == null) {
            log.error("Неполадки с полем {}. Работа программы остановлена. Проверте конфигурационный файл {}", pPropName, pathToProperties);
            System.exit(0);
        } else if (nPropValue.isEmpty()) {
            log.error("Не указана информация в поле {}. Проверте конфигурационный файл {}", pPropName, pathToProperties);
            System.exit(0);
        }

        log.info("{} = {}", pPropName, nPropValue);
        return nPropValue;
    }

    public String getPathToProperties() {
        return pathToProperties;
    }

    public String getFileName() {
        String s = bimFile.trim();
        return s.substring(0, s.length() - 5);
    }

    public DistributionType getBimDistribution() {
        return distribution;
    }

    public double getD0() {
        return d0;
    }

    public double getK() {
        return k;
    }

    public int getNormativeDistributionType() {
        return normativeType == NormativeDistributionType.UNIFORM ? NormativeDistribution.UNIFORM : NormativeDistribution.RANDOM;
    }

    public int getDistributionSeed() {
        return seed;
    }

    public String getResultsFolder() {
        return resultsFolder;
    }

    public String getCfastFile() {
        return cfastFile;
    }

    public Double getBeginEvacuation() {
        return beginEvacuation;
    }

    public boolean isFire() {
        return isFire;
    }
}
