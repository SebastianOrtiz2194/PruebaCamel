package prueba.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import prueba.dtos.User;

/**
 * A simple Camel route that triggers from a timer and calls a bean and prints
 * to system out.
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */
@Component
public class MySpringBootRouter extends RouteBuilder {
	private JacksonDataFormat jsonUser = new JacksonDataFormat(User.class);

	@Override
    public void configure() {
    	
    	restConfiguration()
    	.component("servlet")
        .enableCORS(true)
        .port(8080)
        .contextPath("/api")
        .bindingMode(RestBindingMode.auto)
    	.corsHeaderProperty("Access-Control-Allow-Origin", "*")
    	.corsHeaderProperty("Access-Control-Allow-Headers","*");
        
    	rest("users")
    		.get("/listar").outType(User.class)
    		.consumes(MediaType.APPLICATION_JSON_VALUE.toString())
    		.produces(MediaType.APPLICATION_JSON_VALUE.toString())
    		.route()
    		.to("sql:select * from user?dataSource=#dataSource")
    		.log("${body}");
    	
    	rest("users")
			.get("/{id}").outType(User.class)
			.consumes(MediaType.APPLICATION_JSON_VALUE.toString())
			.produces(MediaType.APPLICATION_JSON_VALUE.toString())
			.route()
			.to("sql:select * from user where id=:#id?dataSource=#dataSource")
    		.log("${body}");
    	
    
    	rest("users")
			.post("/guardar").type(User.class)
			.consumes(MediaType.APPLICATION_JSON_VALUE.toString())
			.produces(MediaType.APPLICATION_JSON_VALUE.toString())
			.route()
			.setHeader("id",simple("${body.id}")).setHeader("name",simple("${body.name}")).setHeader("age",simple("${body.age}"))
			.to("sql:insert into user (id, name, age) values (:#id, :#name, :#age)?dataSource=#dataSource")
			.log("${body}")
			.setHeader("CamelFileName", simple("${body.name}.txt"))
			.log("${body}")
			.marshal(jsonUser)
			.log("${body}")
    		.log("${headers}")
	        .to("file:/files")
    		.log("${headers}")
    		.log("${header.CamelFileName}")
    		.unmarshal(jsonUser)
    		.log("${body}");
    	
    	
    	rest("users")
			.delete("/{id}").type(User.class)
			.consumes(MediaType.APPLICATION_JSON_VALUE.toString())
			.produces(MediaType.APPLICATION_JSON_VALUE.toString())
			.route()
			.to("sql:delete from user where id=:#id?dataSource=#dataSource")
			.log("${body}");
    	
    	rest("users")
			.put("/{id}").type(User.class)
			.consumes(MediaType.APPLICATION_JSON_VALUE.toString())
			.produces(MediaType.APPLICATION_JSON_VALUE.toString())
			.route().setHeader("name",simple("${body.name}")).setHeader("age",simple("${body.age}"))
			.log("${header.name}, ${header.age}")
			.to("sql:update user set name=:#name, age=:#age where id=:#id?dataSource=#dataSource")
			.log("${body}");
    	
    	rest("users")
			.patch("/{id}").type(User.class)
			.consumes(MediaType.APPLICATION_JSON_VALUE.toString())
			.produces(MediaType.APPLICATION_JSON_VALUE.toString())
			.route().streamCaching()
				.setHeader("name",simple("${body.name}"))
				.setHeader("age",simple("${body.age}"))
				.setHeader("CamelSqlQuery",simple("update user set params where id=:#id"))
				.process("replaceQuery")
				.log("${header.CamelSqlQuery}")
				.to("sql:query?dataSource=#dataSource");
	
    }

}
