package com.pmdm.rest_con_json;

// código adaptado por Ramón José Martínez Cuevas del código original de la página web:
// http://picarcodigo.blogspot.com.es/2014/05/webservice-conexion-base-de-datos-mysql.html

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

// recuerda que debes añadir el permiso de internet al manifiesto para poder acceder a
// los scripts de php y mandar información por POST mediante HTTP
// <uses-permission android:name="android.permission.INTERNET" />

public class MainActivity extends Activity {

    // dirección IP o URL del servidor
    //private final static String URL_SERVIDOR ="192.168.0.14"; // comprobar IP local que puede cambiar
    private final static String URL_SERVIDOR = "fmolina.esy.es"; // hosting en hostinger
    // URL del directorio de los scripts php del servidor
    private final static String URL_PHP = "http://" + URL_SERVIDOR + "/rest_con_json/";
    // atributos
    public EditText matricula;
    public EditText alumno;
    public EditText telefono;
    public EditText email;
    private int id;
    private Button insertar, mostrar, eliminar, actualizar;
    private ImageButton mas, menos;
    private int posicion = 0;  // posición del alumno a mostrar de la lista de alumnos
    private List<Alumno> listaAlumnos = null; // Lista de alumnos obtenidos de la BD
    private ProgressDialog pDialog = null; // barra de progreso (mostrada mientras se conecta a la BD)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // establacer que la orientación del dispositivo sea siempre vertical
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        setContentView(R.layout.activity_main);

        // acceder a los cuadros de texto
        matricula = (EditText) findViewById(R.id.matricula);
        alumno = (EditText) findViewById(R.id.nombre);
        telefono = (EditText) findViewById(R.id.telefono);
        email = (EditText) findViewById(R.id.email);

        // crear la lista de alumnos
        listaAlumnos = new ArrayList<Alumno>();

        // acceder al botón insertar
        insertar = (Button) findViewById(R.id.insertar);
        //Definir la acción del botón Insertar => Insertamos los datos del alumno
        insertar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //controlar que la información no esté en blanco
                if (!matricula.getText().toString().trim().equalsIgnoreCase("") ||
                        !alumno.getText().toString().trim().equalsIgnoreCase("") ||
                        !telefono.getText().toString().trim().equalsIgnoreCase("") ||
                        !email.getText().toString().trim().equalsIgnoreCase(""))

                    // intentar insertar los datos del alumno con el servicio web
                    new WebService_insertar(MainActivity.this).execute();

                else
                    tostada("Hay información por rellenar");
            }

        });


        // acceder al botón mostrar
        mostrar = (Button) findViewById(R.id.mostrar);
        //Definir la acción del botón Mostrar =>Mostramos los datos de la persona por pantalla
        mostrar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // mostrar el alumno
                new WebService_mostrar(MainActivity.this).execute();
            }
        });

        // acceder al botón mostrar
        eliminar = (Button) findViewById(R.id.eliminar);
        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listaAlumnos.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No existen alumnos en la base de datos", Toast.LENGTH_SHORT).show();
                } else {
                    final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                    alert.setTitle("¿Desea borrar el alumno?")
                            .setPositiveButton("Borrar",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            new WebService_borrar(MainActivity.this).execute();
                                        }
                                    })
                            .setNegativeButton("Cancelar",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                        }
                                    });
                    alert.show();
                }
            }
        });

        // acceder al botón mostrar
        actualizar = (Button) findViewById(R.id.actualizar);
        //Definir la acción del botón Mostrar =>Mostramos los datos de la persona por pantalla
        actualizar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // mostrar el alumno
                if (listaAlumnos.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No existen alumnos en la base de datos", Toast.LENGTH_SHORT).show();
                } else {
                    new WebService_actualizar(MainActivity.this).execute();

                }
            }
        });


        // acceder al botón más => +
        mas = (ImageButton) findViewById(R.id.mas);
        // Definir la acción del botón + => Se mueve por nuestro ArrayList mostrando el alumno siguiente
        mas.setOnClickListener(new View.OnClickListener() {

            // la lista de alumnos va desde la posición 0 hasta el tamaño-1 => size()-1

            @Override
            public void onClick(View v) {
                // Comprobar si la lista de alumnos no está vacía
                if (!listaAlumnos.isEmpty()) {

                    if (posicion >= listaAlumnos.size() - 1)
                        // se ha alcanzando o superado el final de lista
                        // posición debe valer el final de la lista por si se ha superado el valor
                        posicion = listaAlumnos.size() - 1;
                    else
                        // no se ha alcanzando o superado el final de lista => avanzar
                        posicion++;

                    // mostrar el alumno de la lista situado en posición
                    mostrarAlumno(posicion);
                }
            }

        });

        // Se mueve por nuestro ArrayList mostrando el objeto anterior
        menos = (ImageButton) findViewById(R.id.menos);
        // Definir la acción del botón - => Se mueve por nuestro ArrayList mostrando el alumno anterior
        menos.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Comprobar si la lista de alumnos no está vacía
                if (!listaAlumnos.isEmpty()) {
                    if (posicion <= 0)
                        // se ha alcanzando el principio de lista o posición tiene valor negativo
                        // posición debe valer el principio de la lista por si tiene valor negativo
                        posicion = 0;
                    else
                        // no se ha alcanzando el principio de lista => retroceder
                        posicion--;

                    // mostrar el alumno de la lista situado en posición
                    mostrarAlumno(posicion);
                }
            }
        });


    } // fin onCreate()

    // Intenta insertar los datos de las Personas en el servidor
    // a través del script => insert.php
    // devuelve true => sin la inserción es correcta
    // devuelve false => si hubo un error en la inserción
    private boolean insertar() {
        boolean resul = false;

        // interfaz para un cliente HTTP
        HttpClient httpclient;
        // define una lista de parámetros ("clave" "valor") que serán enviados por POST al script php
        List<NameValuePair> parametros_POST;
        // define un objeto para realizar una solicitud POST a través de HTTP
        HttpPost httppost;
        // crea el cliente HTTP
        httpclient = new DefaultHttpClient();
        // creamos el objeto httpost para realizar una solicitud POST al script insert.php
        httppost = new HttpPost(URL_PHP + "insert.php"); // Url del Servidor

        /* Cuando estamos trabajando de manera local el ida y vuelta será casi inmediato
         * para darle un poco realismo decimos que el proceso se pare por unos segundos para poder
		 * observar el progressdialog, la podemos eliminar si queremos
		 */
        // SystemClock.sleep(950); // dormir el proceso actual 950 milisegundos

        //Añadimos nuestros datos que vamos a enviar por POST al script insert.php
        parametros_POST = new ArrayList<NameValuePair>(4);
        parametros_POST.add(new BasicNameValuePair("alumno", alumno.getText().toString().trim()));
        parametros_POST.add(new BasicNameValuePair("matricula", matricula.getText().toString().trim()));
        parametros_POST.add(new BasicNameValuePair("telefono", telefono.getText().toString().trim()));
        parametros_POST.add(new BasicNameValuePair("email", email.getText().toString().trim()));

        try {
            // establece la entidad => como una lista de pares URL codificada.
            // Esto suele ser útil al enviar una solicitud HTTP POST
            httppost.setEntity(new UrlEncodedFormEntity(parametros_POST));
            // intentamos ejecutar la solicitud HTTP POST
            httpclient.execute(httppost);
            resul = true;
        } catch (UnsupportedEncodingException e) {
            // La codificación de caracteres no es compatible
            resul = false;
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // Señala un error en el protocolo HTTP
            resul = false;
            e.printStackTrace();
        } catch (IOException e) {
            // Error de Entrada / Salida
            resul = false;
            e.printStackTrace();
        }

        // devuelve el resultado de la inserción
        return resul;

    } // fin insertar()


    // Realiza una consulta a la BD de todas las matriculas de los alumnos
    // a través del script => selectAll.php
    // Devuelve los datos del servidor en forma de String
    private String mostrar() {

        // almacenará la respuesta del servidor BD
        String resultado = "";

        // crea el cliente HTTP por defecto
        HttpClient httpclient = new DefaultHttpClient();

        // creamos el objeto httpost para realizar una solicitud POST al script insert.php
        HttpPost httppost = new HttpPost(URL_PHP + "selectAll.php"); // Url del Servidor

        HttpResponse response;
        try {
            //ejecuto petición enviando datos por POST
            response = httpclient.execute(httppost);
            // obtiene la entidad del mensaje de respuesta HTTP
            HttpEntity entity = response.getEntity();
            // crea un nuevo flujo de entrada tipo InputStream => instream => con la entidad HTTP => entity
            InputStream instream = entity.getContent();
            // convierte la respuesta del servidor => instream => a formato cadena (String) => resultado
            resultado = convertStreamToString(instream);
        } catch (ClientProtocolException e) {
            // error en el protocolo HTTP
            e.printStackTrace();
        } catch (IOException e) {
            // error de E/S
            e.printStackTrace();
        }

        return resultado;

    } // fin mostrar()


    // Intenta borrar los datos de las Personas en el servidor
    // a través del script => delete.php
    // devuelve true => sin la eliminación es correcta
    // devuelve false => si hubo un error en la eliminación
    private boolean borrar() {
        boolean resul = false;

        // interfaz para un cliente HTTP
        HttpClient httpclient;
        // define una lista de parámetros ("clave" "valor") que serán enviados por POST al script php
        List<NameValuePair> parametros_POST;
        // define un objeto para realizar una solicitud POST a través de HTTP
        HttpPost httppost;
        // crea el cliente HTTP
        httpclient = new DefaultHttpClient();
        // creamos el objeto httpost para realizar una solicitud POST al script insert.php
        httppost = new HttpPost(URL_PHP + "delete.php"); // Url del Servidor

        /* Cuando estamos trabajando de manera local el ida y vuelta será casi inmediato
         * para darle un poco realismo decimos que el proceso se pare por unos segundos para poder
		 * observar el progressdialog, la podemos eliminar si queremos
		 */
        // SystemClock.sleep(950); // dormir el proceso actual 950 milisegundos

        //Añadimos nuestros datos que vamos a enviar por POST al script delete.php
        parametros_POST = new ArrayList<NameValuePair>();

        // Extrae del alumno seleccionado a través de su posición, el id
        // y se pasa como parámetro POST al script
        Alumno alumno = listaAlumnos.get(posicion);
        String id = String.valueOf(alumno.getId());
        parametros_POST.add(new BasicNameValuePair("id", id));

        try {
            // establece la entidad => como una lista de pares URL codificada.
            // Esto suele ser útil al enviar una solicitud HTTP POST
            httppost.setEntity(new UrlEncodedFormEntity(parametros_POST));
            // intentamos ejecutar la solicitud HTTP POST
            httpclient.execute(httppost);
            resul = true;
        } catch (UnsupportedEncodingException e) {
            // La codificación de caracteres no es compatible
            resul = false;
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // Señala un error en el protocolo HTTP
            resul = false;
            e.printStackTrace();
        } catch (IOException e) {
            // Error de Entrada / Salida
            resul = false;
            e.printStackTrace();
        }

        // devuelve el resultado de la inserción
        return resul;
    } // fin borrar()

    // Inserta los datos de las Personas en el servidor
    // a través del script => update.php
    // devuelve true => sin la actualización es correcta
    // devuelve false => si hubo un error en la actualización
    private boolean actualizar() {
        boolean resul = false;

        // interfaz para un cliente HTTP
        HttpClient httpclient;
        // define una lista de parámetros ("clave" "valor") que serán enviados por POST al script php
        List<NameValuePair> parametros_POST;
        // define un objeto para realizar una solicitud POST a través de HTTP
        HttpPost httppost;
        // crea el cliente HTTP
        httpclient = new DefaultHttpClient();
        // creamos el objeto httpost para realizar una solicitud POST al script insert.php
        httppost = new HttpPost(URL_PHP + "update.php"); // Url del Servidor

        /* Cuando estamos trabajando de manera local el ida y vuelta será casi inmediato
         * para darle un poco realismo decimos que el proceso se pare por unos segundos para poder
		 * observar el progressdialog, la podemos eliminar si queremos
		 */
        // SystemClock.sleep(950); // dormir el proceso actual 950 milisegundos

        //Añadimos nuestros datos que vamos a enviar por POST al script update.php
        parametros_POST = new ArrayList<NameValuePair>(5);
        parametros_POST.add(new BasicNameValuePair("alumno", alumno.getText().toString().trim()));
        parametros_POST.add(new BasicNameValuePair("matricula", matricula.getText().toString().trim()));
        parametros_POST.add(new BasicNameValuePair("telefono", telefono.getText().toString().trim()));
        parametros_POST.add(new BasicNameValuePair("email", email.getText().toString().trim()));

        Alumno alumno = listaAlumnos.get(posicion);
        String id = String.valueOf(alumno.getId());
        parametros_POST.add(new BasicNameValuePair("id", id));

        try {
            // establece la entidad => como una lista de pares URL codificada.
            // Esto suele ser útil al enviar una solicitud HTTP POST
            httppost.setEntity(new UrlEncodedFormEntity(parametros_POST));
            // intentamos ejecutar la solicitud HTTP POST
            httpclient.execute(httppost);
            resul = true;
        } catch (UnsupportedEncodingException e) {
            // La codificación de caracteres no es compatible
            resul = false;
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // Señala un error en el protocolo HTTP
            resul = false;
            e.printStackTrace();
        } catch (IOException e) {
            // Error de Entrada / Salida
            resul = false;
            e.printStackTrace();
        }

        // devuelve el resultado de la actualización
        return resul;

    } // fin actualizar()

    // convierte la respuesta del servidor => is => a formato cadena (String) => y la devuelve
    private String convertStreamToString(InputStream is) throws IOException {

        String resul = ""; // resultado a devolver
        BufferedReader reader = null;

        //Convierte respuesta a String
        try {
            // crear un flujo de entrada de tipo BufferedReader en base
            // a un flujo de entrada InputStreamReader con un juego de caracteres de tipo "UTF-8"
            // el tamaño del buffer es de 8 caracteres
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);

            // quién no sepa que es un StringBuilder =>
            // http://picandocodigo.net/2010/java-stringbuilder-stringbuffer/

            // crea una cadena de caracteres modificable => StringBuilder
            StringBuilder sb = new StringBuilder();

            // lee todas las líneas del fichero a través del flujo de entrada reader
            String line = null;
            while ((line = reader.readLine()) != null)
                // añade cada línea leída del fichero con un salto de línea => "\n"
                sb.append(line + "\n");

            // guardamos el resultado de la respuesta en el String => result
            resul = sb.toString();

            Log.e("getpostresponse", " result= " + sb.toString());

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("log_tag", "Error E/S al convertir el resultado " + e.toString());

        } finally {
            // la clausula finally siempre se ejecuta => salten excepciones o no
            // por eso es conveniente intentar cerrar aquí los flujos => por si hay un error
            // en la lectura del flujo por ejemplo de tipo E/S => IOException
            try {
                if (is != null)
                    is.close(); // cerrar el flujo de entrada is
                if (reader != null)
                    reader.close(); // cerrar el flujo de entrada reader
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("log_tag", "Error E/S al cerrar los flujos de entrada " + e.toString());
            }

        }

        return resul;

    } // fin convertStreamToString()


    // Descompone, crea un objeto con los datos descompuestos y lo almacena en nuestro ArrayList
    // devuelve true => si hay algún alumno que mostrar
    //          false => en caso contrario
    private boolean filtrarDatos() {
        boolean resul = false;
        listaAlumnos.clear();
        Alumno alumno = null;

        String respuesta = mostrar();

        // Compara respuesta ignorando mayúsculas y minúsculas con la cadena ""
        if (!respuesta.equalsIgnoreCase("")) {

            JSONObject json; // define un objeto JSON

            boolean error_json = false; // para detectar un error al transformar a JSON

            try {
                // crea el objeto JSON en base al String respuesta
                json = new JSONObject(respuesta);
                // devuelve un array json si existe el índice "alumnos"
                JSONArray jsonArray = json.optJSONArray("alumnos");
                for (int i = 0; i < jsonArray.length(); i++) {
                    alumno = new Alumno();
                    // obtener el objeto JSON de la posición i
                    JSONObject jsonArrayChild = jsonArray.getJSONObject(i);

                    // guardar el alumno jsonArrayChild en el objeto alumno
                    alumno.setId(jsonArrayChild.optInt("id"));
                    alumno.setAlumno(jsonArrayChild.optString("nombre"));
                    alumno.setMatricula(jsonArrayChild.optInt("matricula"));
                    alumno.setTelefono(jsonArrayChild.optInt("telefono"));
                    alumno.setEmail(jsonArrayChild.optString("email"));

                    // añadir el alumno a la lista de alumnos
                    listaAlumnos.add(alumno);
                }

            } catch (JSONException e) {
                // Error al convertir a JSON
                // => esto sucede porque no hay alumnos en la consulta y el arrya json está vacío
                // o por cualquie otro motivo
                e.printStackTrace();
                error_json = true;
            }

            if (error_json)
                resul = false;
            else
                resul = true;
        } else
            resul = false;

        return resul;
    } // fin filtrarDatos()

    //Muestra el alumno almacenado en nuestro ArrayList listaAlumnos
    private void mostrarAlumno(int posicion) {
        // recoger en alumno2 la información del alumno ubicado en la posición ("posicion") de listaAlumnnos
        Alumno alumno2 = listaAlumnos.get(posicion);
        // poner la información del alumno en los cuadros de texto
        alumno.setText(alumno2.getAlumno());
        matricula.setText("" + alumno2.getMatricula());
        telefono.setText("" + alumno2.getTelefono());
        email.setText(alumno2.getEmail());
    } // fin mostrarAlumno()

    // muestra una tostada
    public void tostada(String mensaje) {
        Toast toast1 = Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT);
        toast1.show();
    }

    /*		CLASE ASYNCTASK
    *
	* usaremos esta para poder mostrar el dialogo de progreso mientras enviamos y obtenemos los datos
	* podria hacerse lo mismo sin usar esto pero si el tiempo de respuesta es demasiado lo que podria ocurrir
	* si la conexion es lenta o el servidor tarda en responder la aplicacion será inestable.
	* ademas observariamos el mensaje de que la app no responde.
	*
	* Este Web Service permitirá insertar un alumno en la BD
	*/
    class WebService_insertar extends AsyncTask<String, String, String> {

        private Activity context;

        WebService_insertar(Activity context) {
            this.context = context;
        }

        /* Proceso Invocado en la Interfaz de Usuario (IU) antes de ejecutar la tarea en segundo plano.
           En este caso, muestra una barra de progreso
		 */
        protected void onPreExecute() {
            // Crea la barra de progreso si es necesario
            if (pDialog == null)
                pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Conectando a la Base de Datos....");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        // Tarea a realizar en segundo plano (con otro hilo que no está en el Interfaz de Usuario)
        // por lo tanto esta tarea no puede interaccionar con el usuario
        @Override
        protected String doInBackground(String... params) {
            String resultado = "ERROR";

            if (insertar())
                // la inserción del alumno ha sido exitosa
                resultado = "OK";
            else
                // ha habido un error al insertar el alumno y no se pudo insertar
                resultado = "ERROR";

            return resultado;
        }

        /* Una vez terminado doInBackground según lo que haya ocurrido
           intentamos mostrar la tostada de que se pudo o no insertar el alumno */
        protected void onPostExecute(String result) {

            pDialog.dismiss();//ocultamos barra de progreso

            if (result.equals("OK")) {
                // inserción correcta
                tostada("Alumno insertado con éxito");
                alumno.setText("");
                matricula.setText("");
                telefono.setText("");
                email.setText("");
            } else
                tostada("ERROR, no se pudo insertar el alumno");
        } // fin onPostExecute()

    } // fin clase WebService_insertar


    /* Este Web Service permitirá mostrar un alumno de la BD
    */
    class WebService_mostrar extends AsyncTask<String, String, String> {

        private Activity context;

        WebService_mostrar(Activity context) {
            this.context = context;
        }

        /* Proceso Invocado en la Interfaz de Usuario (IU) antes de ejecutar la tarea en segundo plano.
           En este caso, muestra una barra de progreso
		 */
        protected void onPreExecute() {
            // Crea la barra de progreso si es necesario
            if (pDialog == null)
                pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Conectando a la Base de Datos....");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        // Tarea a realizar en segundo plano (con otro hilo que no está en el Interfaz de Usuario)
        // por lo tanto esta tarea no puede interaccionar con el usuario
        @Override
        protected String doInBackground(String... params) {
            String resultado = "ERROR";

            if (filtrarDatos()) {
                // hay un alumno que mostrar
                resultado = "OK";
            } else
                // no hay alumno que mostrar
                resultado = "ERROR";

            return resultado;
        }

        /* Una vez terminado doInBackground según lo que haya ocurrido
           intentamos mostrar el alumno */
        protected void onPostExecute(String result) {

            pDialog.dismiss();//ocultamos barra de progreso

            if (result.equals("OK")) {
                // se puede mostrar el alumno
                mostrarAlumno(posicion);
                //tostada("muestra alumno");
            } else
                tostada("ERROR, no hay más alumnos que mostrar");
        } // fin onPostExecute()

    } // fin clase WebService_mostrar


    /* Este Web Service permitirá eliminar un alumno de la BD
   */
    class WebService_borrar extends AsyncTask<String, String, String> {

        private Activity context;

        WebService_borrar(Activity context) {
            this.context = context;
        }

        /* Proceso Invocado en la Interfaz de Usuario (IU) antes de ejecutar la tarea en segundo plano.
           En este caso, muestra una barra de progreso
		 */
        protected void onPreExecute() {
            // Crea la barra de progreso si es necesario
            if (pDialog == null)
                pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Conectando a la Base de Datos....");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        // Tarea a realizar en segundo plano (con otro hilo que no está en el Interfaz de Usuario)
        // por lo tanto esta tarea no puede interaccionar con el usuario
        @Override
        protected String doInBackground(String... params) {
            String resultado = "ERROR";

            if (borrar()) {
                // hay un alumno que eliminar
                resultado = "OK";
            } else
                // no hay alumno que eliminar
                resultado = "ERROR";

            return resultado;
        }

        /* Una vez terminado doInBackground según lo que haya ocurrido
           intentamos mostrar el alumno */
        protected void onPostExecute(String result) {

            pDialog.dismiss();//ocultamos barra de progreso

            if (result.equals("OK")) {
                // inserción correcta
                tostada("Alumno eliminado con éxito");
                alumno.setText("");
                matricula.setText("");
                telefono.setText("");
                email.setText("");
            } else
                tostada("ERROR, no se pudo eliminar el alumno");
        } // fin onPostExecute()

    } // fin clase WebService_borrar

    /*
    * Este Web Service permitirá actualizar un alumno en la BD
	*/
    class WebService_actualizar extends AsyncTask<String, String, String> {

        private Activity context;

        WebService_actualizar(Activity context) {
            this.context = context;
        }

        /* Proceso Invocado en la Interfaz de Usuario (IU) antes de ejecutar la tarea en segundo plano.
           En este caso, muestra una barra de progreso
		 */
        protected void onPreExecute() {
            // Crea la barra de progreso si es necesario
            if (pDialog == null)
                pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Conectando a la Base de Datos....");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        // Tarea a realizar en segundo plano (con otro hilo que no está en el Interfaz de Usuario)
        // por lo tanto esta tarea no puede interaccionar con el usuario
        @Override
        protected String doInBackground(String... params) {
            String resultado = "ERROR";

            if (actualizar())
                // la inserción del alumno ha sido exitosa
                resultado = "OK";
            else
                // ha habido un error al insertar el alumno y no se pudo insertar
                resultado = "ERROR";

            return resultado;
        }

        /* Una vez terminado doInBackground según lo que haya ocurrido
           intentamos mostrar la tostada de que se pudo o no insertar el alumno */
        protected void onPostExecute(String result) {

            pDialog.dismiss();//ocultamos barra de progreso

            if (result.equals("OK")) {
                // inserción correcta
                tostada("Alumno modificado con éxito");
                alumno.setText("");
                matricula.setText("");
                telefono.setText("");
                email.setText("");
            } else
                tostada("ERROR, no se pudo modificar el alumno");
        } // fin onPostExecute()

    } // fin clase WebService_insertar
}