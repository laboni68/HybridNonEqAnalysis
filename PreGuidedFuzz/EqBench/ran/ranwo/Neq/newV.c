#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include <stdbool.h>
double snippet(int idum){//&idum
		const int IM1=2147483563,IM2=2147483399;
		const int IA1=40014,IA2=40692,IQ1=53668,IQ2=52774;
		const int IR1=12211,IR2=3791,NTAB=32,IMM1=IM1-1;
		const int NDIV=1+IMM1/NTAB;
		const double EPS=3.0e-16,RNMX=1.0-EPS,AM=1.0/(double)(IM1);
		int idum2=123456789,iy=0;
		int iv[NTAB];
		int j,k;
		double temp;

		if (idum <= 0) {
			idum=(idum==0 ? 1 : -idum);
			idum2=idum;
			for (j=NTAB+7;j>=0;j--) {
				k=idum/IQ1;
				idum=IA1*(idum-k*IQ1)-k*IR1;
				if (idum < 0) idum += IM1;
				if (j < NTAB) iv[j] = idum;
			}
			iy=iv[0];
		}
		k=idum/IQ1;
		idum=IA1*(idum-k*IQ1)-k*IR1;
		if (idum < 0 || idum == 0) idum += IM1;//change
        	idum *= IMM1;//change
		k=idum2/IQ2;
		idum2=IA2*(idum2-k*IQ2)-k*IR2;
		if (idum2 < 0) idum2 += IM2;
		j=iy/NDIV;
		iy=iv[j]-idum2;
		iv[j] = idum;
		if (iy < 1) iy += IMM1;
		if ((temp=AM*iy) < RNMX) return RNMX;//change
		else return temp;
    }