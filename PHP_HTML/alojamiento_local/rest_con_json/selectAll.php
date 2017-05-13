<?php
	$hostname_localhost ="localhost";  //nuestro servidor
	$database_localhost ="alumnos"; //Nombre de nuestra base de datos
	$username_localhost ="root";       //Nombre de usuario de nuestra base de datos (yo utilizo el valor por defecto)
	$password_localhost ="";   //Contraseña de nuestra base de datos

	// Conexión a nuestro servidor mysql
	$localhost = mysql_connect($hostname_localhost,$username_localhost,$password_localhost)
	or
	trigger_error(mysql_error(),E_USER_ERROR); //mensaje de error si no se puede conectar

	//selección de la base de datos con la que se desea trabajar	
	mysql_select_db($database_localhost, $localhost);
	
	// realizar una consulta que selecciona todas las matriculas de los alumnos
	// ordenadas por la matricula
	$query_search = "select * from matriculas order by matricula";

	// lanzar la consulta
	$query_exec = mysql_query($query_search) 
	or 
	die(mysql_error()); // en caso de que haya un error en la consulta

	// crear un array para almacenar el json
	$json = array();
	
	// comprobar si hay al menos una fila como resultado de la consulta
	if(mysql_num_rows($query_exec)){

		// Devuelve un array asociativo que corresponde a la fila recuperada y mueve el puntero de datos interno hacia adelante
		while($row=mysql_fetch_assoc($query_exec)){
			// almacena cada fila de la consulta en el array $json
			$json['alumnos'][]=$row;
		}
	}

	// cerrar la conexión con la BD mysql
	mysql_close($localhost);

	// mostrar por pantalla y codificar el vector $json como JSON
	echo json_encode($json);
	
?>


