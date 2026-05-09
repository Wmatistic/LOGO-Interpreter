grammar logo;

prog
    : stmt* EOF
    ;

stmt
    : 'fd' expr                                     #fd
    | 'bk' expr                                     #bk
    | 'rt' expr                                     #rt
    | 'lt' expr                                     #lt
    | 'pu'                                          #pu
    | 'pd'                                          #pd
    | 'hm'                                          #hm
    | 'sc' expr                                     #sc
    | 'sw' expr                                     #sw
    | 'pc' expr expr expr                           #pc
    | 'setx' expr                                   #setx
    | 'sety' expr                                   #sety
    | 'bf'                                          #bf
    | 'ef'                                          #ef
    | 'rp' expr '[' stmt* ']'                       #repeat
    | 'make' VAR expr                               #make
    | 'if' expr '[' stmt* ']'
      ('else' '[' stmt* ']')?                       #ifStmt
    | 'while' expr '[' stmt* ']'                    #whileStmt
    | 'to' ID PARAM* stmt* 'end'                    #procDef
    | ID expr*                                      #procCall
    ;

expr
    : left=expr op=('>'|'<'|'>='|'<='|'=='|'!=') right=expr   #compare
    | left=expr op=('+'|'-') right=expr                         #addSub
    | left=expr op=('*'|'/') right=expr                         #mulDiv
    | '-' expr                                                   #negate
    | '(' expr ')'                                               #paren
    | PARAM                                                      #varRef
    | FLOAT                                                      #float
    | INT                                                        #int
    ;

PARAM   : ':' [a-zA-Z][a-zA-Z0-9_]* ;
VAR     : '"' [a-zA-Z][a-zA-Z0-9_]* ;
ID      : [a-zA-Z][a-zA-Z0-9_]* ;
FLOAT   : [0-9]+ '.' [0-9]* | '.' [0-9]+ ;
INT     : [0-9]+ ;
WS      : [ \t\r\n]+ -> skip ;
COMMENT : ';' ~[\r\n]* -> skip ;
