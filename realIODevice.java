import java.util.LinkedList;
import java.util.Queue;

public class realIODevice implements IODevice
{
	private int ID;
	private String name;
	private long freeTime;
	public Queue<ProcessControlBlock> deviceQueue = new LinkedList<ProcessControlBlock>();

	public realIODevice(int ID, String name)
	{
		this.ID = ID;
		this.name = name;
		freeTime = 0;
	}

	//add pcb that needs the io device
	public void addPCB(ProcessControlBlock pcb)
	{
		deviceQueue.add(pcb);

	}

	@Override
	public int getID()
	{
		// TODO Auto-generated method stub
		return ID;
	}

	@Override
	public String getName()
	{
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public long getFreeTime()
	{
		for (ProcessControlBlock pcb : deviceQueue)
		{
			freeTime += pcb.getInstruction().getDuration();
		}

		return freeTime;
	}

	@Override
	public long requestIO(int duration, ProcessControlBlock process)
	{
		// TODO Auto-generated method stub
		deviceQueue.add(process);
		return 0;
	}

}
