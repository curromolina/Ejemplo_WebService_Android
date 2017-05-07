<?php
	//delete.php

	$hostname ="mysql.hostinger.es";  //nuestro servidor de BD
	$database ="u392158408_alum"; //Nombre de nuestra base de datos
	$username ="u392158408_molin"; //Nombre de usuario de nuestra base de datos 
	$password ="";   //Contraseña de nuestra base de datos	

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