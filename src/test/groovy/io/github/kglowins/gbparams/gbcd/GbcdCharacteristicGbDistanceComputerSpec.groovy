package io.github.kglowins.gbparams.gbcd

import io.github.kglowins.gbparams.core.BoundaryCharacterizer
import io.github.kglowins.gbparams.core.SymmetryOperations
import io.github.kglowins.gbparams.enums.BoundaryParameterName
import io.github.kglowins.gbparams.enums.PointGroup
import io.github.kglowins.gbparams.enums.PolarDistributionCharacteristicsName
import io.github.kglowins.gbparams.representation.AxisAngle
import io.github.kglowins.gbparams.representation.Matrix3x3
import io.github.kglowins.gbparams.representation.UnitVector
import org.apache.commons.csv.CSVPrinter
import spock.lang.Specification

import static io.github.kglowins.gbparams.io.csv.BoundaryParameterCsvUtil.withCombinedHeader
import static io.github.kglowins.gbparams.io.csv.BoundaryParameterCsvUtil.withMinimalBoundaryParamsHeader
import static io.github.kglowins.gbparams.io.csv.PolarDistributionCsvUtil.withPolarDistributionHeader


class GbcdCharacteristicGbDistanceComputerSpec extends Specification {

    def "should compute"() {

        given:
        GbcdCharacteristicGbDistanceComputer computer = new GbcdCharacteristicGbDistanceComputer(4000)
            .with(BoundaryParameterName.TILT_AM)
        UnitVector v = new UnitVector()
        v.set(1,1,1)
        AxisAngle aa = new AxisAngle()
        aa.set(v, Math.toRadians(60))
        Matrix3x3 M = new Matrix3x3()
        M.set(aa)
        Iterable<PolarDistributionValue> dist = computer.compute(M, PointGroup.M3M)

        FileWriter fileWriter = new FileWriter("test.csv");
        CSVPrinter csvPrinter = new CSVPrinter(fileWriter, withPolarDistributionHeader())
        dist.each {
            csvPrinter.printRecord(it.getPolar(), it.getAzimuth(),it.getStereographicX(),
                    it.getStereographicY(),it.get(BoundaryParameterName.TILT_AM));
        }

        expect:
        true
    }

}