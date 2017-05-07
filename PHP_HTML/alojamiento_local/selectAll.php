<?php
	// REST CON JSON Hostinger --- selectAll.php

	$hostname_localhost ="localhost";  //nuestro servidor
	$database_localhost ="alumnos"; //Nombre de nuestra base de datos
	$username_localhost ="root";       //Nombre de usuario de nuestra base de datos (yo utilizo el valor por defecto)
	$password_localhost ="";   //Contraseña de nuestra base de datos
	
	// intentar conectar al servidor con el usuario y contraseña anteriores
	$mysqli = new mysqli($hostname,$username,$password, $database);

	/*
	 * This is the "official" OO way to do it,
	 * BUT $connect_error was broken until PHP 5.2.9 and 5.3.0.
	 */
	if ($mysqli->connect_error) {
		die('Connect Error (' . $mysqli->connect_errno . ') '
			    . $mysqli->connect_error);
	}	
	
 	
	// realizar una consulta que selecciona todas las matriculas de los alumnos
	// ordenadas por la matricula
	$query_search = "select * from matriculas order by matricula";

	// crear un array para almacenar el json
	$json = array();

	// lanzar la consulta
	$query_exec = $mysqli->query($query_search);
	if (!$query_exec) {
		echo "Falló la consulta de la tabla matriculas: (" . $mysqli->errno . ") " . $mysqli->error;
	}
	else {		
		while($row = $query_exec->fetch_array(MYSQLI_ASSOC)){
			// almacena cada fila de la consulta en el array $json
			$json['alumnos'][]=$row;
		}		
		
	}		

	// cerrar la conexión con la BD mysql
	$mysqli->close();	

	// mostrar por pantalla y codificar el vector $json como JSON
	echo json_encode($json);	
	
?>


