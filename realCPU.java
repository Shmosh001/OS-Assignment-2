public class realCPU implements CPU
{
	ProcessControlBlock pcb;
	realKernel kernel;
	int cs; //context switches

	public void getKernel(realKernel k)
	{
		kernel = k;
	}

	public int getCS()
	{
		return cs;
	}

	@Override
	public ProcessControlBlock getCurrentProcess()
	{
		// TODO Auto-generated method stub
		return pcb;
	}

	@Override
	//execute process by a given timeslice
	public int execute(int timeUnits)
	{
		int timeUnitsRemain;
		Instruction instr = pcb.getInstruction();
		//execute instruction. Return time remaining
		timeUnitsRemain = ((CPUInstruction) instr).execute(timeUnits);

		//extra time units left or zero
		if (timeUnitsRemain >= 0)
		{
			simulatorDriver.sysTimer.advanceUserTime(timeUnits - timeUnitsRemain);
			boolean hasNextInstruct = pcb.hasNextInstruction();
			//still has a next instruction
			if (hasNextInstruct == true)
			{
				pcb.nextInstruction();
				instr = pcb.getInstruction();
				IOInstruction ioInstr = (IOInstruction) instr;
				pcb.setState(ProcessControlBlock.State.WAITING);
				kernel.syscall(SystemCall.IO_REQUEST, ioInstr.getDeviceID(), ioInstr.getDuration());

				return timeUnitsRemain;

			}

			//no more instructions
			else
			{
				pcb.setState(ProcessControlBlock.State.TERMINATED);
				kernel.syscall(SystemCall.TERMINATE_PROCESS);
				return timeUnitsRemain;
			}

		}

		//unfinished instruction. negative timeUnits remaining returned
		else
		{
			simulatorDriver.sysTimer.advanceUserTime(timeUnits);

			pcb.setState(ProcessControlBlock.State.READY);
			return 0;
		}

	}

	@Override
	//Context switch 
	public ProcessControlBlock contextSwitch(ProcessControlBlock process)
	{

		cs++; //increment context switch counter
		ProcessControlBlock oldPCB = getCurrentProcess(); //retrieve current process
		this.pcb = process; //set new process
		System.out.printf("Time: %010d Context Switch(%s , %s) \n", simulatorDriver.sysTimer.getSystemTime(), oldPCB,
				this.pcb);

		return oldPCB;

	}

	@Override
	public boolean isIdle()
	{
		if (getCurrentProcess() == null)
		{
			return true;
		}

		else
		{
			return false;
		}

	}

}
