#ifndef _PARSER_COMMON_H_
#define _PARSER_COMMON_H_

#include <string>
#include <vector>


extern int (*printfIfDebugging) (const char* format, ...);
int yywrap();

//Function used when parsing with boost::spirit
void copyVectorOfCharsToString(const std::vector<char>& input, std::string* result);


#endif
