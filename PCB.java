import java.util.ArrayList;
import java.util.List;

public class PCB implements ProcessControlBlock
{

	private int PID;
	private String ProgramName;
	private State state;
	List<Instruction> instructList = new ArrayList<Instruction>();
	int pc;

	public PCB(String name, int PID)
	{
		this.PID = PID;
		ProgramName = name;
		setState(State.READY);
		pc = 0;
	}

	@Override
	public int getPID()
	{

		// TODO Auto-generated method stub
		return PID;
	}

	@Override
	public String getProgramName()
	{
		// TODO Auto-generated method stub
		return ProgramName;
	}

	@Override
	public Instruction getInstruction()
	{
		// TODO Auto-generated method stub
		return instructList.get(pc);
	}

	@Override
	public void nextInstruction()
	{
		pc++;
		// TODO Auto-generated method stub

	}

	public boolean hasNextInstruction()
	{
		if (pc + 1 >= instructList.size())
		{
			return false;
		}

		else
		{
			return true;
		}

	}

	public List<Instruction> getInstructionList()
	{
		return instructList;
	}

	public int getProgramCounter()
	{
		return this.pc;
	}

	@Override
	public State getState()
	{

		// TODO Auto-generated method stub
		return state;
	}

	@Override
	public void setState(State state)
	{
		this.state = state;

	}

	public void equals(ProcessControlBlock pcb)
	{
		this.PID = pcb.getPID();
		this.ProgramName = pcb.getProgramName();
		this.state = pcb.getState();
		this.instructList.equals(pcb.getInstructionList());
		this.pc = pcb.getProgramCounter();
	}

	public String toString()
	{
		return String.format("{%d, %s}", this.getPID(), this.getProgramName());
	}

}
