package io.github.kglowins.gbparams.io.csv;

import io.github.kglowins.gbparams.enums.BoundaryParameterName;
import io.github.kglowins.gbparams.enums.PointGroup;
import io.github.kglowins.gbparams.representation.EulerAngles;
import io.github.kglowins.gbparams.representation.Matrix3x3;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import static io.github.kglowins.gbparams.core.SymmetryOperations.getSymmetryOperations;
import static java.lang.Double.MAX_VALUE;
import static java.lang.Double.parseDouble;
import static java.util.stream.StreamSupport.stream;

@Slf4j
public class BoundaryParameterCsvUtil {

  private BoundaryParameterCsvUtil() {
  }

  public static CSVFormat withMinimalBoundaryParamsHeader() {
    return CSVFormat.DEFAULT.withHeader(
            BoundaryParameterName.LEFT_PHI1.getCsvHeader(),
            BoundaryParameterName.LEFT_PHI.getCsvHeader(),
            BoundaryParameterName.LEFT_PHI2.getCsvHeader(),
            BoundaryParameterName.RIGHT_PHI1.getCsvHeader(),
            BoundaryParameterName.RIGHT_PHI.getCsvHeader(),
            BoundaryParameterName.RIGHT_PHI2.getCsvHeader(),
            BoundaryParameterName.POLAR.getCsvHeader(),
            BoundaryParameterName.AZIMUTH.getCsvHeader(),
            BoundaryParameterName.PHASE_ID.getCsvHeader(),
            BoundaryParameterName.POINT_GROUP.getCsvHeader(),
            BoundaryParameterName.AREA.getCsvHeader(),
            BoundaryParameterName.FACES.getCsvHeader()
    );
  }

  public static Map<BoundaryParameterName, Boolean> getPresentHeaders(String csvPath)
      throws IOException {
    Map<String, Integer> headersIndexes = getHeadersAndIndexes(csvPath);
    Map<BoundaryParameterName, Boolean> parametersPresent = new EnumMap<>(BoundaryParameterName.class);
    Stream.of(BoundaryParameterName.values()).forEach(param ->
        parametersPresent.put(param, headersIndexes.containsKey(param.getCsvHeader()))
    );
    return parametersPresent;
  }

  public static Map<String, Integer> getHeadersAndIndexes(String csvPath) throws IOException {
    Reader in = new FileReader(csvPath);
    CSVParser parser = CSVParser.parse(in, CSVFormat.DEFAULT.withFirstRecordAsHeader());
    Map<String, Integer> headerMap = parser.getHeaderMap();
    headerMap.forEach((h, i) -> log.debug("getHeadersAndIndexes: {} -> {}", h, i));
    return headerMap;
  }

  public static PointGroup getPointGroupFromRecord(String csvPointGroup) {
    return Stream.of(PointGroup.values())
        .filter(pg -> pg.getLabel().equals(csvPointGroup)).findFirst().get();
  }

  public static CSVFormat withCombinedHeader(Map<BoundaryParameterName, Boolean> parametersPresence,
      Set<BoundaryParameterName> parametersToCompute) {
    String[] headersToWrite = Stream.of(BoundaryParameterName.values())
        .filter(param -> parametersPresence.get(param) || parametersToCompute.contains(param))
        .map(BoundaryParameterName::getCsvHeader)
        .toArray(String[]::new);
    Stream.of(headersToWrite).forEach(h -> log.debug("withCombinedHeader: {}", h));
    return CSVFormat.DEFAULT.withHeader(headersToWrite);
  }

  public static int getNumberOfRecordsAfterHeader(String csvPath) throws IOException {
    Reader in = new FileReader(csvPath);
    Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
    int numberOfRecords = 0;
    for (CSVRecord record : records) {
      numberOfRecords++;
    }
    log.debug("Number of Records: {}", numberOfRecords);
    return numberOfRecords;
  }

  public static PointGroup readPointGroupFromFirstRecord(String csvPath) throws IOException {
    Reader in = new FileReader(csvPath);
    Iterator<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in).iterator();
    if (records.hasNext()) {
      String pointGroupInCsvCell = records.next().get(BoundaryParameterName.POINT_GROUP.getCsvHeader());
      return getPointGroupFromRecord(pointGroupInCsvCell);
    } else {
      throw new IOException(
          String.format("Could not read point group from 1st row of %s", csvPath));
    }
  }

  public static double readDouble(CSVRecord record, BoundaryParameterName param) {
    return parseDouble(record.get(param.getCsvHeader()));
  }

  public static double computeDisorientationForRecord(CSVRecord record) {
    double phi1L = readDouble(record, BoundaryParameterName.LEFT_PHI1);
    double PhiL = readDouble(record, BoundaryParameterName.LEFT_PHI);
    double phi2L = readDouble(record, BoundaryParameterName.LEFT_PHI2);
    double phi1R = readDouble(record, BoundaryParameterName.RIGHT_PHI1);
    double PhiR = readDouble(record, BoundaryParameterName.RIGHT_PHI);
    double phi2R = readDouble(record, BoundaryParameterName.RIGHT_PHI2);
    String csvPointGroup = record.get(BoundaryParameterName.POINT_GROUP.getCsvHeader());
    PointGroup pointGroup = getPointGroupFromRecord(csvPointGroup);

    List<Matrix3x3> symmetryOperations = getSymmetryOperations(pointGroup);

    EulerAngles eulL = new EulerAngles();
    eulL.set(phi1L, PhiL, phi2L);
    Matrix3x3 ML = new Matrix3x3();
    ML.set(eulL);

    EulerAngles eulR = new EulerAngles();
    eulR.set(phi1R, PhiR, phi2R);
    Matrix3x3 MR = new Matrix3x3();
    MR.set(eulR);

    Matrix3x3 M = new Matrix3x3(ML);
    M.timesTransposed(MR);

    double minimalDisorientation = MAX_VALUE;
    for (Matrix3x3 C : symmetryOperations) {
      Matrix3x3 CM = new Matrix3x3(M);
      CM.leftMul(C);
      double disorientation = CM.rotationAngle();
      if (disorientation < minimalDisorientation) {
        minimalDisorientation = disorientation;
      }
    }
    return minimalDisorientation;
  }

  public static List<Double> getColumnAsList(String csvPath, BoundaryParameterName param)
      throws IOException{
    return stream(CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(new FileReader(csvPath)).spliterator(), false)
        .map(record -> readDouble(record, param))
        .collect(Collectors.toList());
  }
}
