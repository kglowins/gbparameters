package io.github.kglowins.gbparams.gbcd;

import io.github.kglowins.gbparams.core.BoundaryCharacterizer;
import io.github.kglowins.gbparams.core.SymmetryOperations;
import io.github.kglowins.gbparams.enums.BoundaryParameterName;
import io.github.kglowins.gbparams.enums.PointGroup;
import io.github.kglowins.gbparams.representation.InterfaceMatrix;
import io.github.kglowins.gbparams.representation.Matrix3x3;
import io.github.kglowins.gbparams.representation.UnitVector;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.Accessors;
import org.apache.commons.math3.util.FastMath;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.kglowins.gbparams.enums.BoundaryParameterName.*;
import static java.lang.Math.PI;

public class GbcdCharacteristicGbDistanceComputer {

    private Set<BoundaryParameterName> distributionsToCompute;

   // @Getter
    private List<PolarCoordinates> coordinates;

   // @Getter
   // @Setter
   // private AtomicInteger progress;

   // @Getter
   // @Setter
   // private AtomicBoolean isCancelled;

    public GbcdCharacteristicGbDistanceComputer(int numberOfPoints) {
        coordinates = new ArrayList<>();
        distributionsToCompute = new HashSet<>();

        final int nPts = 2 * numberOfPoints;
        final double margin = 4. / Math.sqrt(nPts);
        final double p = 0.5;
        final double a = 1. - 2. * p / (nPts - 3);
        final double b = p * (nPts + 1) / (nPts - 3);
        final double C = 3.6 / Math.sqrt(nPts);
        final double D = 1. / (nPts - 1);

        double rLast = 0.;
        double phiLast = 0.;
        for (int k = 2; k < nPts; k++) {
            final double kPrime = a * k + b;
            final double hk = -1. + 2. * (kPrime - 1) * D;
            final double rk = Math.sqrt(1. - hk * hk);
            final double theta_k = FastMath.acos(hk);
            final double phi_k = Math.IEEEremainder(phiLast + C * 2. / (rLast + rk), 2. * PI);
            if (theta_k <= 0.5 * PI + margin) {
                coordinates.add(new PolarCoordinates(theta_k, phi_k));
            }
            rLast = rk;
            phiLast = phi_k;
        }
        coordinates.add(new PolarCoordinates(0, 0));
    }

    public GbcdCharacteristicGbDistanceComputer with(BoundaryParameterName param) {
        distributionsToCompute.add(param);
        return this;
    }

    public GbcdCharacteristicGbDistanceComputer with(Set<BoundaryParameterName> params) {
        distributionsToCompute = params;
        return this;
    }

    public Iterable<PolarDistributionValue> compute(Matrix3x3 fixedM, PointGroup pointGroup) {


        //AtomicInteger processedCount = new AtomicInteger(0);
     //   isCancelled = new AtomicBoolean(false);
      //  progress = new AtomicInteger(0);

        return Flowable.fromIterable(coordinates)
               // .takeWhile(coords -> !isCancelled.get())
                .parallel(16)
                .runOn(Schedulers.computation())
                .map(coords -> {
                    //System.out.println(coords);
                    BoundaryCharacterizer characterizer = new BoundaryCharacterizer()
                            .compute(distributionsToCompute)
                            .symmetryOperations(SymmetryOperations.getSymmetryOperations(pointGroup));

                    UnitVector m1 = new UnitVector();
                    m1.set(coords.polar(), coords.azimuth());
                    InterfaceMatrix b0 = new InterfaceMatrix(fixedM, m1);

                    characterizer.characterize(b0);

                    Map<BoundaryParameterName, Double> values = new EnumMap<>(BoundaryParameterName.class);

                    PolarDistributionValue distributionValue = new PolarDistributionValue(coords.polar(), coords.azimuth());
                //    if (distributionsToCompute.contains(TILT_AM)) {
                        distributionValue.set(TILT_AM, characterizer.getDistanceToTiltAm());
              //      }
             /*       if (distributionsToCompute.contains(TWIST_AM)) {
                        distributionValue.set(TWIST_AM, characterizer.getDistanceToTwistAm());
                    }
                    if (distributionsToCompute.contains(SYMMETRIC_AM)) {
                        distributionValue.set(SYMMETRIC_AM, characterizer.getDistanceToSymmetricAm());
                    }
                    if (distributionsToCompute.contains(TILT_180_AM)) {
                        distributionValue.set(TILT_180_AM, characterizer.getDistanceTo180tiltAm());
                    }

                    if (distributionsToCompute.contains(TILT_APRX)) {
                        distributionValue.set(TILT_APRX, characterizer.distanceToTiltAprx());
                    }
                    if (distributionsToCompute.contains(TWIST_APRX)) {
                        distributionValue.set(TWIST_APRX, characterizer.distanceToTwistAprx());
                    }
                    if (distributionsToCompute.contains(SYMMETRIC_APRX)) {
                        distributionValue.set(SYMMETRIC_AM, characterizer.distanceToSymmetricAprx());
                    }
                    if (distributionsToCompute.contains(TILT_180_APRX)) {
                        distributionValue.set(TILT_180_APRX, characterizer.distanceTo180tiltAprx());
                    }

                    if (distributionsToCompute.contains(TILT_COMP)) {
                        distributionValue.set(TILT_COMP, characterizer.tiltComponent());
                    }
                    if (distributionsToCompute.contains(TWIST_COMP)) {
                        distributionValue.set(TWIST_COMP, characterizer.twistComponent());
                    }
*/
                 //   int incremented = processedCount.incrementAndGet();
                //    if (incremented % 50 == 0){
                //        System.out.println(incremented);
                //    }
                  //  progress.set((int)(100. * incremented / coordinates.size()));
                    return distributionValue;
                })
                .sequential()
                .blockingIterable();
    }

    @Value
    @Accessors(fluent = true)
    private static class PolarCoordinates {
        double polar;
        double azimuth;
    }
}
