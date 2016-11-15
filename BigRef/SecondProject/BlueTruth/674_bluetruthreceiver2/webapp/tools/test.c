#define a 48271 
#define m 2147483647
#define q (m / a)
#define r (m % a)
#include <stdio.h>

static unsigned int seed = 1;

long int PMrand()
{
   seed = a *seed% m;
   return seed;
}

int main() {
   int i = 0;
   for (;i<10;i++){
	seed=(a*seed)%m;
      printf("[%d] %x - %d\n",i+1,seed&0xFFFF,seed&0xFFFF);
   }
   return 0;
}
