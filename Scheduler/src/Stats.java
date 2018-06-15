

import java.text.DecimalFormat;

public class Stats
{
double cpu_perc,mem_perc;double mem_usage,mem_limit;
double net_i,net_o,block_i,block_o;
double mem_total,mem_avail;
int total_core,sockets;
double n_cpu_perc,n_mem_usage,n_neti,n_neto,n_blocki,n_blocko; //Normailise cpu and mem usage
double mem_dist[];
int machine_id;

public Stats(double cpu_perc, double mem_perc, int mem_usage, int mem_limit) {
	super();
	this.cpu_perc = cpu_perc;
	this.mem_perc = mem_perc;
	this.mem_usage = mem_usage;
	this.mem_limit = mem_limit;
	n_blocki=0;n_blocko=0;
	n_neti=0;n_neto=0;
	
}
public double getCpu_perc() {
	return cpu_perc;
}
public void setCpu_perc(double cpu_perc) {
	this.cpu_perc = cpu_perc;
}
public double getMem_perc() {
	return mem_perc;
}
public void setMem_perc(double mem_perc) {
	this.mem_perc = mem_perc;
}
public double getMem_usage() {
	return mem_usage;
}
public void setMem_usage(int mem_usage) {
	this.mem_usage = mem_usage;
}
public Stats() {
	super();
}
public double getLimit() {
	return mem_limit;
}
public void setLimit(double limit) {
	this.mem_limit = limit;
}
public double convert_to_Mb(String a)
{
	DecimalFormat numberFormat = new DecimalFormat("#.0000");
	String unit=a.substring(a.indexOf(" "),a.length()).trim();
	double data =Double.parseDouble(a.substring(0,a.indexOf(" ")).trim()); 
	if(unit.equals("MiB") || unit.equalsIgnoreCase("MB")) {
		
		//System.out.println("Unit is" +unit);
	}
	else if(unit.equals("GiB") || unit.equalsIgnoreCase("GB") )
	{
		data=data*1024;
	}
	else if(unit.equals("KiB") || unit.equalsIgnoreCase("KB"))
	{
		data=data/1024;
	}
	else if(unit.equalsIgnoreCase("B"))
	{
		data=data/(1024*1024);
	}
//	System.out.println("Data "+data+" Unit "+unit);
	data=Double.parseDouble(numberFormat.format(data));
//	System.out.println("Data "+data+" Unit "+unit);
	return data;
}
public double[] gen_mem_dist(String s,int sockets)
{
	double d[]=new double[sockets];//System.out.println();
	String a[]=s.split(" ");
	for(int i=0;i<sockets;i++)
	{
		d[i]=Double.parseDouble(a[i+1].trim().substring(a[i+1].indexOf("=")+1, a[i+1].length()));
		//System.out.print("Numa accees "+i+" "+d[i]);
	}//System.out.println();
	return d;
}
public void convertStats(String a[])
{
	
	this.cpu_perc=Double.parseDouble(a[2].trim().substring(0, a[2].trim().length()-1));
	this.mem_perc=Double.parseDouble(a[4].trim().substring(0,a[4].trim().length()-1));
	
	this.mem_usage=convert_to_Mb(a[3].substring(0, a[3].indexOf("/")).trim());
	this.mem_limit=convert_to_Mb(a[3].substring(a[3].indexOf("/")+1,a[3].length()).trim());
	
	this.net_i=convert_to_Mb(a[5].substring(0, a[5].indexOf("/")).trim());
	this.net_o=convert_to_Mb(a[5].substring(a[5].indexOf("/")+1,a[5].length()).trim());
	
	this.block_i=convert_to_Mb(a[6].substring(0, a[6].indexOf("/")).trim());
	this.block_o=convert_to_Mb(a[6].substring(a[6].indexOf("/")+1,a[6].length()).trim());
	
	this.mem_total=Integer.parseInt(a[7].trim())/(1024);
	this.mem_avail=Integer.parseInt(a[8].trim())/(1024);
	
	this.total_core=Integer.parseInt(a[9].trim());
	this.sockets=Integer.parseInt(a[10].trim());
	
	
	this.mem_dist=new double[sockets];
	this.mem_dist=gen_mem_dist(a[11].trim(),sockets);
	

	
	
}
}