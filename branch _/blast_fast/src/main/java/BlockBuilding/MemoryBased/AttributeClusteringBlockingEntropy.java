package BlockBuilding.MemoryBased;

import BlockBuilding.AbstractAttributeClusteringBlocking;
import BlockBuilding.AbstractAttributeClusteringBlockingEntropy;
import DataStructures.EntityProfile;
import Utilities.RepresentationModel;

import java.util.List;

public class AttributeClusteringBlockingEntropy extends AbstractAttributeClusteringBlockingEntropy {

    public AttributeClusteringBlockingEntropy(RepresentationModel md, List<EntityProfile>[] profiles) {
        super(md, profiles);
    }

    public AttributeClusteringBlockingEntropy(RepresentationModel md, List<EntityProfile>[] profiles, int minhash_size, int rows, boolean approx) {
        super(md, profiles, minhash_size, rows, approx);
    }

    @Override
    protected void setDirectory() {
        setMemoryDirectory();
    }
}
