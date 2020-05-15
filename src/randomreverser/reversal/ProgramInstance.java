package randomreverser.reversal;

import randomreverser.reversal.calltype.CallType;
import randomreverser.reversal.constraint.Constraint;
import randomreverser.reversal.instruction.Instruction;
import randomreverser.reversal.observation.Observation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

public class ProgramInstance {

    private final Program program;
    private final List<Observation> observations = new ArrayList<>();
    private int callIndex = 0;

    protected ProgramInstance(Program program) {
        this.program = program;
    }

    public Program getProgram() {
        return program;
    }

    @SuppressWarnings("unchecked")
    public <T> ProgramInstance add(Object value) {
        CallType<T> callType = (CallType<T>) program.getCalls().get(callIndex++);
        T thing = callType.getType().cast(value);
        callType.addObservations(thing, observations);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <O extends Observation> O getObservation(Constraint<O> constraint) {
        // TODO: better error checking
        return (O) observations.get(program.getConstraintIndex(constraint));
    }

    public LongStream reverse() {
        LongStream stream = null;
        for (Instruction instruction : program.getInstructions()) {
            stream = instruction.filter(this, stream);
        }
        return stream;
    }

}
