package io.github.kglowins.gbparams.io.dream3d;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import io.github.kglowins.gbparams.enums.PointGroup;

import static io.github.kglowins.gbparams.enums.PointGroup.*;
import static ncsa.hdf.object.FileFormat.*;


public class Dream3DFile {

    private FileFormat fileFormat;

    public static final Map<Integer, PointGroup> CRYSTAL_STRUCTURE_POINT_GROUP_MAP
        = Map.of(
            0, _6MMM,
            1, M3M,
            2, MMM);

    private Dream3DFile(String dream3DFilePath) throws Exception {
        fileFormat = getFileFormat(FILE_TYPE_HDF5);
        fileFormat = fileFormat.createInstance(dream3DFilePath, READ);
        fileFormat.open();
    }

    public static Dream3DFile open(String dream3DFilePath) throws Exception {
        return new Dream3DFile(dream3DFilePath);
    }

    public static PointGroup pointGroupFromCrystalStructure(int crystalStructureId) {
        return CRYSTAL_STRUCTURE_POINT_GROUP_MAP.get(crystalStructureId);
    }

    public Dream3DDataSetPaths tryGuessDataSetPaths() {
        return Dream3DDataSetPaths.builder()
            .phaseCrystalStructures(tryMatchDefaultPath("CrystalStructures"))
            .grainPhases(tryMatchDefaultPath("Grain Data/Phases"))
            .surfaceGrains(tryMatchDefaultPath("SurfaceFeatures"))
            .grainEulerAngles(tryMatchDefaultPath("AvgEulerAngles"))
            .faceNormals(tryMatchDefaultPath("FaceNormals"))
            .faceGrainIds(tryMatchDefaultPath("FaceLabels"))
            .faceAreas(tryMatchDefaultPath("FaceAreas"))
            .nodeTypes(tryMatchDefaultPath("NodeType"))
            .faceNodes(tryMatchDefaultPath("SharedTriList"))
            .nodeCoordinates(tryMatchDefaultPath("SharedVertexList"))
            .build();
    }

    public Object readDataSet(String dataSetPath) throws Exception {
        Dataset dataset = (Dataset) fileFormat.get(dataSetPath);
        return dataset.read();
    }

    public Set<String> getDataSetPaths() {
        Set<String> dataSetPaths = new LinkedHashSet<>();
        Group root = (Group) ((DefaultMutableTreeNode) fileFormat.getRootNode()).getUserObject();
        walkGroupAndSavePaths(root, dataSetPaths);
        return dataSetPaths;
    }

    private static void walkGroupAndSavePaths(Group group, Set<String> dataSetPaths) {
        group.getMemberList().forEach(member -> {
            if (member instanceof Group) {
                Group subgroup = (Group) member;
                walkGroupAndSavePaths(subgroup, dataSetPaths);
            } else {
                String dataSetPath = member.getFullName();
                if (!dataSetPath.contains("Pipeline")) {
                    dataSetPaths.add(member.getFullName());
                }
            }
        });
    }

    private String tryMatchDefaultPath(String defaultPathSubstring) {
        return getDataSetPaths().stream()
            .filter(path -> path.contains(defaultPathSubstring))
            .findFirst().orElse("");
    }
}
