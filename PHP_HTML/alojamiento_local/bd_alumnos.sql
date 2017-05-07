create database if not exists alumnos
DEFAULT CHARACTER SET utf8 
COLLATE utf8_bin ;
use alumnos;
create table matriculas (
nombre varchar(10) not null, 
matricula Int not null, 
telefono Int not null, 
email varchar(30) not null, 
id Int primary key auto_increment
);