public class realSystemTimer implements SystemTimer
{
	long sysTime;
	long idleTime;
	long userTime;
	long kernelTime;
	int timeSlice;
	int overhead;
	int numCS;

	public realSystemTimer()
	{
		sysTime = 0;
		idleTime = 0;
		userTime = 0;
		kernelTime = 0;
		numCS = 0;
	}

	public void setTimeSlice(int timeSlice)
	{
		this.timeSlice = timeSlice;
	}

	public void setOverhead(int overhead)
	{
		this.overhead = overhead;
	}

	public int getOverhead()
	{
		return overhead;
	}

	@Override
	public long getSystemTime()
	{
		// TODO Auto-generated method stub
		return sysTime;
	}

	@Override
	public long getIdleTime()
	{
		return idleTime;
	}

	@Override
	public long getUserTime()
	{
		// TODO Auto-generated method stub
		return userTime;
	}

	@Override
	public long getKernelTime()
	{
		// TODO Auto-generated method stub
		return kernelTime;
	}

	@Override
	public void setSystemTime(long systemTime)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void advanceSystemTime(long time)
	{
		// TODO Auto-generated method stub
		sysTime += time;

	}

	@Override
	public void advanceUserTime(long time)
	{
		// TODO Auto-generated method stub
		userTime += time;
		advanceSystemTime(time);

	}

	@Override
	public void advanceKernelTime(long time)
	{
		// TODO Auto-generated method stub
		kernelTime += time;
		advanceSystemTime(time);

	}

	public void advanceIdleTime(long time)
	{
		// TODO Auto-generated method stub
		idleTime += time;
		advanceSystemTime(time);

	}

	@Override
	//used for Time_Out events only
	public void scheduleInterrupt(int timeUnits, PCB pcb)
	{
		TimeOutEvent timeOut = new TimeOutEvent(timeUnits, pcb);
		simulatorDriver.eventQ.add(timeOut);

	}

	@Override
	//cancel a time_out Interrupt 
	public void cancelInterrupt(PCB pcb)
	{
		//remove time_out interrupt related to given pcb
		simulatorDriver.eventQ.remove(pcb);
	}
}
