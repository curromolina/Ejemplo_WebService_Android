<?php
	//delete.php

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

	// variables que almacenan los valores que enviamos por nuestra app por POST o a través de un formulario
	// observar que se llaman igual en nuestra app y aquí
	$id = $_POST['id'];

	// realizar la inserción de los datos del alumno en la tabla matriculas
	$borrar = "delete from matriculas where id = '$id'";
	
	// lanzar la consulta
	$query_exec = $mysqli->query($borrar);
	if (!$query_exec) {
		echo "Falló la eliminación de la tabla matriculas: (" . $mysqli->errno . ") " . $mysqli->error;
	}	 

?>