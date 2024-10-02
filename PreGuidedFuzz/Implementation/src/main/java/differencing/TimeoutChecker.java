package differencing;

import differencing.classification.Classification;
import differencing.models.Iteration;
import differencing.models.Partition;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;

import java.util.Set;

public class TimeoutChecker extends ListenerAdapter {
    private final DifferencingListener diffListener;
    private final Iteration iteration;
    private final int timeout;
    public boolean partionReached = false;

    public TimeoutChecker(DifferencingListener diffListener, Iteration iteration, int timeout) {
        this.diffListener = diffListener;
        this.iteration = iteration;
        this.timeout = timeout;
    }

    public boolean timeoutReached() {
        float time = StopWatches.getTime("iteration-" + this.iteration.iteration);

        Set<Partition> partitions = diffListener.getPartitions();
        boolean hasNeqPartition = partitions.stream().anyMatch(p -> p.result == Classification.NEQ);
        boolean hasNonEqPartition = partitions.stream().anyMatch(p -> p.result != Classification.EQ);

        // End the run if:
        // 1.     the timeout has been exceeded
        // 2. AND we have at least one non-EQ partition
        // 3. AND we have no NEQ partition.
        //System.out.println("timeout check: " + time + " timeout: " + timeout + " partitions: " + partitions.size() + " hasNonEqPartition: " + hasNonEqPartition + " hasNeqPartition: " + hasNeqPartition);
        return time > timeout && ((partitions.size() == 0) || (hasNonEqPartition && !hasNeqPartition));
        // return (time > timeout && ((partitions.size() == 0) || (hasNonEqPartition && !hasNeqPartition))) || (partitions.size() >=50);

        // We do NOT want to interrupt runs that have ONLY EQ partitions
        // because they might turn out to be EQ
        // but just require more time to prove this.

        // We do NOT want to interrupt runs that have NEQ partitions
        // because we already know they are NEQ,
        // so we want to collect more data for them.

        // We DO want to interrupt all other runs (i.e., runs that
        // do NOT have NEQ partitions but DO have undecided partitions)
        // because we already know we can't prove them to be EQ (some partitions are undecided)
        // but we have no good reason to think that we will find NEQ partitions for them either
        // if we were not able to do so until now.
    }

    public boolean partitionReached() {
        float time = StopWatches.getTime("iteration-" + this.iteration.iteration);

        Set<Partition> partitions = diffListener.getPartitions();
        boolean hasNeqPartition = partitions.stream().anyMatch(p -> p.result == Classification.NEQ);
        boolean hasNonEqPartition = partitions.stream().anyMatch(p -> p.result != Classification.EQ);

        // End the run if:
        // 1.     the timeout has been exceeded
        // 2. AND we have at least one non-EQ partition
        // 3. AND we have no NEQ partition.
        //System.out.println("timeout check: " + time + " timeout: " + timeout + " partitions: " + partitions.size() + " hasNonEqPartition: " + hasNonEqPartition + " hasNeqPartition: " + hasNeqPartition);
        return partitions.size() >=50;

        // We do NOT want to interrupt runs that have ONLY EQ partitions
        // because they might turn out to be EQ
        // but just require more time to prove this.

        // We do NOT want to interrupt runs that have NEQ partitions
        // because we already know they are NEQ,
        // so we want to collect more data for them.

        // We DO want to interrupt all other runs (i.e., runs that
        // do NOT have NEQ partitions but DO have undecided partitions)
        // because we already know we can't prove them to be EQ (some partitions are undecided)
        // but we have no good reason to think that we will find NEQ partitions for them either
        // if we were not able to do so until now.
    }

    @Override
    public void stateAdvanced (Search search) {
        if (timeoutReached()) {
            search.notifySearchConstraintHit(this.getErrorMessage());
            search.terminate();
        }
        // if (partitionReached()) {
        //     partionReached = true;
        //     search.notifySearchConstraintHit(this.getErrorMessage());
        //     search.terminate();
        // }
    }

    private String getErrorMessage() {
        return "max iteration time exceeded: " + this.timeout + "s";
    }
}
