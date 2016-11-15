#ifndef _EXECUTE_COMMAND_H_
#define _EXECUTE_COMMAND_H_

#include <string>
#include <vector>

#include <unistd.h>


::pid_t popen2(char** command, int* , int* );

typedef std::vector<std::string> TStringArray;
char** createArgVFromVector(const TStringArray& allArgs);
void destroyArgV(char** argv, const size_t nArgs);
int execute(const TStringArray& argvStringArray);

#endif /*_EXECUTE_COMMAND_H_*/
