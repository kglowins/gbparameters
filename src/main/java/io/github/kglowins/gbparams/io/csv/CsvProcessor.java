package io.github.kglowins.gbparams.io.csv;


import io.github.kglowins.gbparams.core.BoundaryCharacterizer;
import io.github.kglowins.gbparams.core.BoundaryCharacteristics;

import io.github.kglowins.gbparams.enums.BoundaryParameterName;
import io.github.kglowins.gbparams.enums.PointGroup;
import io.github.kglowins.gbparams.representation.EulerAngles;
import io.github.kglowins.gbparams.representation.InterfaceMatrix;
import io.github.kglowins.gbparams.representation.Matrix3x3;
import io.github.kglowins.gbparams.representation.UnitVector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;


import javax.swing.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;


import static io.github.kglowins.gbparams.core.SymmetryOperations.getSymmetryOperations;
import static io.github.kglowins.gbparams.enums.BoundaryParameterName.*;
import static io.github.kglowins.gbparams.io.csv.BoundaryParameterCsvUtil.*;
import static java.time.Instant.now;

@Slf4j
public class CsvProcessor extends SwingWorker<Void, Void> {

  private static final int BATCH_SIZE = 256;

  private String inCsvPath;
  private String outCsvPath;
  private Map<BoundaryParameterName, Boolean> headersPresence;
  private Set<BoundaryParameterName> parametersToCompute;
  private int numberOfThreads;

  public CsvProcessor(String inCsvPath) throws IOException {
    this.inCsvPath = inCsvPath;
    headersPresence = getPresentHeaders(inCsvPath);
    numberOfThreads = 4 * Runtime.getRuntime().availableProcessors();
    log.info("Number of Threads = " + numberOfThreads);
  }

  public void execute(String outCsvPath, Set<BoundaryParameterName> parametersToCompute) {
    this.outCsvPath = outCsvPath;
    this.parametersToCompute = parametersToCompute;
    execute();
  }

  @Override
  protected Void doInBackground() throws Exception {
    Instant startTime = now();
    Reader in = new FileReader(inCsvPath);
    Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
    Iterator<CSVRecord> recordIterator = records.iterator();
    FileWriter fileWriter = new FileWriter(outCsvPath);
    try (CSVPrinter csvPrinter = new CSVPrinter(fileWriter,
        withCombinedHeader(headersPresence, parametersToCompute))) {

      while(recordIterator.hasNext()) {

        CompletionService<Object> completionService = new ExecutorCompletionService<>(
            Executors.newFixedThreadPool(numberOfThreads));
        List<Future> futures = new ArrayList();

        while (recordIterator.hasNext() && futures.size() < BATCH_SIZE) {
          final CSVRecord record = recordIterator.next();
          final long recordId = record.getRecordNumber();

          final double leftPhi1 = Double.parseDouble(record.get(LEFT_PHI1.getCsvHeader()));
          final double leftPhi = Double.parseDouble(record.get(LEFT_PHI.getCsvHeader()));
          final double leftPhi2 = Double.parseDouble(record.get(LEFT_PHI2.getCsvHeader()));
          final double rightPhi1 = Double.parseDouble(record.get(RIGHT_PHI1.getCsvHeader()));
          final double rightPhi = Double.parseDouble(record.get(RIGHT_PHI.getCsvHeader()));
          final double rightPhi2 = Double.parseDouble(record.get(RIGHT_PHI2.getCsvHeader()));
          final double polar = Double.parseDouble(record.get(POLAR.getCsvHeader()));
          final double azimuth = Double.parseDouble(record.get(AZIMUTH.getCsvHeader()));
          final int phaseId = Integer.parseInt(record.get(PHASE_ID.getCsvHeader()));
          final PointGroup pointGroup = getPointGroupFromRecord(record.get(POINT_GROUP.getCsvHeader()));
          final double area = Double.parseDouble(record.get(AREA.getCsvHeader()));
          final int faces = Integer.parseInt(record.get(FACES.getCsvHeader()));

          final EulerAngles leftEulerAngles = new EulerAngles();
          leftEulerAngles.set(leftPhi1, leftPhi, leftPhi2);
          final EulerAngles rightEulerAngles = new EulerAngles();
          rightEulerAngles.set(rightPhi1, rightPhi, rightPhi2);
          final UnitVector normal = new UnitVector();
          normal.set(polar, azimuth);

          final BoundaryCharacteristics boundaryCharacteristics = BoundaryCharacteristics.builder()
              .leftEulerAngles(leftEulerAngles)
              .rightEulerAngles(rightEulerAngles)
              .normal(normal)
              .phaseId(phaseId)
              .pointGroup(pointGroup)
              .area(area)
              .faces(faces)
              .build();

          appendCharacteristicsIfExists(record, TILT_APRX, headersPresence,
              boundaryCharacteristics::setTiltAprx);
          appendCharacteristicsIfExists(record, TWIST_APRX, headersPresence,
              boundaryCharacteristics::setTwistAprx);
          appendCharacteristicsIfExists(record, SYMMETRIC_APRX, headersPresence,
              boundaryCharacteristics::setSymmetricAprx);
          appendCharacteristicsIfExists(record, TILT_180_APRX, headersPresence,
              boundaryCharacteristics::setTilt180Aprx);

          appendCharacteristicsIfExists(record, TILT_AM, headersPresence,
              boundaryCharacteristics::setTiltAM);
          appendCharacteristicsIfExists(record, TWIST_AM, headersPresence,
              boundaryCharacteristics::setTwistAM);
          appendCharacteristicsIfExists(record, SYMMETRIC_AM, headersPresence,
              boundaryCharacteristics::setSymmetricAM);
          appendCharacteristicsIfExists(record, TILT_180_AM, headersPresence,
              boundaryCharacteristics::setTilt180AM);

          futures.add(completionService.submit(() -> {

            final Matrix3x3 ML = new Matrix3x3();
            ML.set(leftEulerAngles);
            final Matrix3x3 MR = new Matrix3x3();
            MR.set(rightEulerAngles);
            final Matrix3x3 M = new Matrix3x3(ML);
            M.timesTransposed(MR);
            normal.transform(ML);
            final InterfaceMatrix B0 = new InterfaceMatrix(M, normal);

            final BoundaryCharacterizer analyzer = new BoundaryCharacterizer()
                    .grainExchange(true)
                    .inversion(true)
                    .symmetryOperations(getSymmetryOperations(PointGroup.M3M))
                    .compute(parametersToCompute);
            analyzer.characterize(B0);

            appendCharacteristicsIfComputed(TILT_APRX, parametersToCompute,
                analyzer.distanceToTiltAprx(),
                boundaryCharacteristics::setTiltAprx);
            appendCharacteristicsIfComputed(TWIST_APRX, parametersToCompute,
                analyzer.distanceToTwistAprx(),
                boundaryCharacteristics::setTwistAprx);
            appendCharacteristicsIfComputed(SYMMETRIC_APRX, parametersToCompute,
                analyzer.distanceToSymmetricAprx(),
                boundaryCharacteristics::setSymmetricAprx);
            appendCharacteristicsIfComputed(TILT_180_APRX, parametersToCompute,
                analyzer.distanceTo180tiltAprx(),
                boundaryCharacteristics::setTilt180Aprx);

            appendCharacteristicsIfComputed(TILT_AM, parametersToCompute,
                analyzer.getDistanceToTiltAm(),
                boundaryCharacteristics::setTiltAM);
            appendCharacteristicsIfComputed(TWIST_AM, parametersToCompute,
                analyzer.getDistanceToTwistAm(),
                boundaryCharacteristics::setTwistAM);
            appendCharacteristicsIfComputed(SYMMETRIC_AM, parametersToCompute,
                analyzer.getDistanceToSymmetricAm(),
                boundaryCharacteristics::setSymmetricAM);
            appendCharacteristicsIfComputed(TILT_180_AM, parametersToCompute,
                analyzer.getDistanceTo180tiltAm(),
                boundaryCharacteristics::setTilt180AM);

            log.debug("Record {} Processed", recordId);

            return boundaryCharacteristics;
          }));
        }

        final int futuresCreated = futures.size();
        log.debug("futuresCreated = {}", futuresCreated);

        int recordsProcessed = 0;

        while (recordsProcessed < futuresCreated && !isCancelled()) {
          try {
            BoundaryCharacteristics boundaryCharacteristics = (BoundaryCharacteristics) completionService
                .take()
                .get();
            csvPrinter.printRecord(boundaryCharacteristics.toCsvRecord());
            recordsProcessed++;
          } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to Process a CSV Record", e);
            return null;
          }
        }
        log.debug("recordsProcessed = {}", recordsProcessed);
      }
    }
    Instant completeTime = now();
    long timeElapsed = Duration.between(startTime, completeTime).getSeconds();
    log.info("timeElapsed: {} seconds", timeElapsed);
    return null;
  }

  @Override
  protected void done() {

  }

  private void appendCharacteristicsIfExists(CSVRecord record, BoundaryParameterName parameter,
                                             Map<BoundaryParameterName, Boolean> isPresent, Consumer<Double> consumer) {
    if (isPresent.get(parameter)) {
      Double csvValue = Double.parseDouble(record.get(parameter.getCsvHeader()));
      consumer.accept(csvValue);
    }
  }

  private void appendCharacteristicsIfComputed(BoundaryParameterName parameter,
                                               Set<BoundaryParameterName> parametersToCompute, double value, Consumer<Double> consumer) {
    if (parametersToCompute.contains(parameter)) {
      consumer.accept(value);
    }
  }
}
