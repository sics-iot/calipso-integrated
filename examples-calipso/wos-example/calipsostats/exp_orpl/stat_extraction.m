a1 = load("delay.rpl.12638");
a2 = load("delay.rpl.28320");
a3 = load("delay.rpl.12681");
a4 = load("delay.rpl.12669");
a5 = load("delay.rpl.32357");
a6 = load("delay.rpl.32340");    


b1 = load("overhead.rpl.12638");
b2 = load("overhead.rpl.28320");
b3 = load("overhead.rpl.12681");
b4 = load("overhead.rpl.12669");
b5 = load("overhead.rpl.32357");
b6 = load("overhead.rpl.32340");    

c1 = load("energy_int.rpl.12638");
c2 = load("energy_int.rpl.28320");
c3 = load("energy_int.rpl.12681");
c4 = load("energy_int.rpl.12669");
c5 = load("energy_int.rpl.32357");
c6 = load("energy_int.rpl.32340");    

d1 = load("pdr.rpl.12638");
d2 = load("pdr.rpl.28320");
d3 = load("pdr.rpl.12681");
d4 = load("pdr.rpl.12669");
d5 = load("pdr.rpl.32357");
d6 = load("pdr.rpl.32340");    



delay = (mean(a1(:,2))+mean(a2(:,2))+mean(a3(:,2))+mean(a4(:,2))+mean(a5(:,2))+mean(a6(:,2)))/6;
save("delay.dat","delay");
TIME_LENGTH  = 2100;
TIME_SPAN = 150;
for i=1:1:TIME_LENGTH/TIME_SPAN
b_mean(i,1) = i*TIME_SPAN - TIME_SPAN/2  ;
	b_mean(i,2)=0;
	occurrence(i)=0;
end

for j=1:1:min([length(b1),length(b2),length(b3),length(b4),length(b5),length(b6)])
for i=1:1:TIME_LENGTH/TIME_SPAN
	
	if (b1(j,1)>(i-1)*TIME_SPAN & b1(j,1) < i*TIME_SPAN)
	b_mean(i,2)+= b1(j,2);
	occurrence(i)++;
	end
	if (b2(j,1)>(i-1)*TIME_SPAN & b2(j,1) < i*TIME_SPAN)
	b_mean(i,2)+= b2(j,2);
	occurrence(i)++;
	end
	if (b3(j,1)>(i-1)*TIME_SPAN & b3(j,1) < i*TIME_SPAN)
	b_mean(i,2)+= b3(j,2);
	occurrence(i)++;
	end
	if (b4(j,1)>(i-1)*TIME_SPAN & b4(j,1) < i*TIME_SPAN)
	b_mean(i,2)+= b4(j,2);
	occurrence(i)++;
	end
	if (b5(j,1)>(i-1)*TIME_SPAN & b5(j,1) < i*TIME_SPAN)
	b_mean(i,2)+= b5(j,2);
	occurrence(i)++;
	end
	if (b6(j,1)>(i-1)*TIME_SPAN & b6(j,1) < i*TIME_SPAN)
	b_mean(i,2)+= b6(j,2);
	occurrence(i)++;
	end
end
end

vect_index=1;
for i=1:1:length(b_mean)
	if (occurrence(i)!=0)
	overhead(vect_index,:)= [b_mean(i,1) b_mean(i,2)/occurrence(i)];
	vect_index++;
	end
end
save("overhead.dat","overhead");


%Energy
for i=1:1:TIME_LENGTH/TIME_SPAN
c_mean(i,1) = i*TIME_SPAN - TIME_SPAN/2  ;
	c_mean(i,2)=0;
	occurrence(i)=0;
end

for j=1:1:min([length(c1),length(c2),length(c3),length(c4),length(c5),length(c6)])
for i=1:1:TIME_LENGTH/TIME_SPAN
	
	if (c1(j,1)>(i-1)*TIME_SPAN & c1(j,1) < i*TIME_SPAN)
	c_mean(i,2)+= c1(j,2);
	occurrence(i)++;
	end
	if (c2(j,1)>(i-1)*TIME_SPAN & c2(j,1) < i*TIME_SPAN)
	c_mean(i,2)+= c2(j,2);
	occurrence(i)++;
	end
	if (c3(j,1)>(i-1)*TIME_SPAN & c3(j,1) < i*TIME_SPAN)
	c_mean(i,2)+= c3(j,2);
	occurrence(i)++;
	end
	if (c4(j,1)>(i-1)*TIME_SPAN & c4(j,1) < i*TIME_SPAN)
	c_mean(i,2)+= c4(j,2);
	occurrence(i)++;
	end
	if (c5(j,1)>(i-1)*TIME_SPAN & c5(j,1) < i*TIME_SPAN)
	c_mean(i,2)+= c5(j,2);
	occurrence(i)++;
	end
	if (c6(j,1)>(i-1)*TIME_SPAN & c6(j,1) < i*TIME_SPAN)
	c_mean(i,2)+= c6(j,2);
	occurrence(i)++;
	end
end
end
vect_index=1;
for i=1:1:length(c_mean)
	if (occurrence(i)!=0)
	energy(vect_index,:)= [c_mean(i,1) c_mean(i,2)/occurrence(i)];
	vect_index++;
	end
end
save("energy.dat","energy");

%PDR
for i=1:1:TIME_LENGTH/TIME_SPAN
d_mean(i,1) = i*TIME_SPAN - TIME_SPAN/2  ;
d_mean(i,2)=0;
occurrence(i)=0;
end

for j=1:1:min([length(d1),length(d2),length(d3),length(d4),length(d5),length(d6)])
for i=1:1:TIME_LENGTH/TIME_SPAN
	
	if (d1(j,1)>(i-1)*TIME_SPAN & d1(j,1) < i*TIME_SPAN)
	d_mean(i,2)+= d1(j,2);
	occurrence(i)++;
	end
	if (d2(j,1)>(i-1)*TIME_SPAN & d2(j,1) < i*TIME_SPAN)
	d_mean(i,2)+= d2(j,2);
	occurrence(i)++;
	end
	if (d3(j,1)>(i-1)*TIME_SPAN & d3(j,1) < i*TIME_SPAN)
	d_mean(i,2)+= d3(j,2);
	occurrence(i)++;
	end
	if (d4(j,1)>(i-1)*TIME_SPAN & d4(j,1) < i*TIME_SPAN)
	d_mean(i,2)+= d4(j,2);
	occurrence(i)++;
	end
	if (d5(j,1)>(i-1)*TIME_SPAN & d5(j,1) < i*TIME_SPAN)
	d_mean(i,2)+= d5(j,2);
	occurrence(i)++;
	end
	if (d6(j,1)>(i-1)*TIME_SPAN & d6(j,1) < i*TIME_SPAN)
	d_mean(i,2)+= d6(j,2);
	occurrence(i)++;
	end
end
end

vect_index=1;
for i=1:1:length(d_mean)
	if (occurrence(i)!=0)
	pdr(vect_index,:)= [d_mean(i,1) d_mean(i,2)/occurrence(i)];
	vect_index++;
	end
end
save("pdr.dat","pdr");

