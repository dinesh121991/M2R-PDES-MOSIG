#include <stdio.h>
#include <stdbool.h>
#include <stdlib.h>

#define CODE_ADDR 1024

enum addr_mode {VAL_REG, REG_REG, ADDR_REG, REG_ADDR};
enum opcode {MOV, ADD, STOP};
char memory[2048], registers[16];
int pc_reg;

void init()
{
    pc_reg = CODE_ADDR;

    int a = pc_reg;
    memory[a++] = MOV;
    memory[a++] = VAL_REG;
    memory[a++] = 4; //value
    memory[a++] = 10; //reg

    memory[a++] = ADD;
    memory[a++] = VAL_REG;
    memory[a++] = 4; //value
    memory[a++] = 10; //reg
}

bool set_register(char value, int regNum) {
    if(regNum > 15)
        return true;
    registers[regNum] = value;
    return false;
}

bool get_register(char* value, int regNum) {
    if(regNum > 15)
        return true;
    *value = registers[regNum];
    return false;
}
bool set_address(char value, int address) {
    if(address == 0 || address >= CODE_ADDR)
        return true;
    memory[address] = value;
    return false;
}
bool get_address(char* value, int address) {
    if(address == 0 || address >= CODE_ADDR)
        return true;
    *value = memory[address];
    return false;
}

void exMOV(enum addr_mode mode, char src, char dst)
{
    char val;
    switch( mode )
    {
        case VAL_REG :
            if(set_register(src, dst)) {
                printf("Bad register access\n");
                exit(-1);
            }
            break;
        case REG_REG:
            if(get_register(&val, src)) {
                printf("Bad register access\n");
                exit(-1);
            }
            if(set_register(val, dst)) {
                printf("Bad register access\n");
                exit(-1);
            }
            break;
        case ADDR_REG :
            if( get_address( &val,src ))
            {
                printf("Bad Address Location -RO\n");
                exit(-1);
            }
            if( set_register( val, dst ))
            {
                printf("Bad Register access \n");
                exit(-1);
            }
            break;
        case REG_ADDR :
            if( get_register( &val,src ))
            {
                printf("Bad Register Access \n");
                exit(-1);
            }
            if( set_address( val,dst))
            {
                printf("Bad Address Location -RO \n");
            }
            break;
        default :
            printf("Bad addressing mode\n");
    }
}

void exADD(enum addr_mode mode, char src,char dst)
{
    char val;
    switch( mode )
    {
        case VAL_REG :
            if(set_register(src, dst)) {
                printf("Bad register access\n");
                exit(-1);
            }
            break;
        case REG_REG:
            if(get_register(&val, src)) {
                printf("Bad register access\n");
                exit(-1);
            }
            if(set_register(val, dst)) {
                printf("Bad register access\n");
                exit(-1);
            }
            break;
        default :
            printf("Bad addressing mode\n");
    }
}

int main()
{
    //int
    init();
    int a = pc_reg;
    while ( memory[a] != STOP )
    {
        switch ( memory[a] )
        {
            case MOV :
                exMOV(memory[a+1],memory[a+2],memory[a+3]);
                break;
            case ADD :
                exADD(memory[a+1],memory[a+2],memory[a+3]);
                break;
            default :
                printf("Please check Your ISA\n");
                return -1;
        }
        a += 4;
    }

    return 0;
}

