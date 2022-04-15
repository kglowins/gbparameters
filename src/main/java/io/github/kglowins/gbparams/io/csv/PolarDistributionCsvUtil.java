package io.github.kglowins.gbparams.io.csv;

import io.github.kglowins.gbparams.enums.PolarDistributionCharacteristicsName;
import org.apache.commons.csv.CSVFormat;

public class PolarDistributionCsvUtil {

    private PolarDistributionCsvUtil() {
    }

    public static CSVFormat withPolarDistributionHeader() {
        return CSVFormat.DEFAULT.withHeader(
                PolarDistributionCharacteristicsName.POLAR.getCsvHeader(),
                PolarDistributionCharacteristicsName.AZIMUTH.getCsvHeader(),
                PolarDistributionCharacteristicsName.STEREOGRAPHIC_X.getCsvHeader(),
                PolarDistributionCharacteristicsName.STEREOGRAPHIC_Y.getCsvHeader(),
                PolarDistributionCharacteristicsName.VALUE.getCsvHeader()
        );
    }
}
