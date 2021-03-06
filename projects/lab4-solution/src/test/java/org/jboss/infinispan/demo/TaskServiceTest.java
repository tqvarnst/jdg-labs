package org.jboss.infinispan.demo;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.infinispan.demo.model.Task;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TaskServiceTest {
	
	Logger log = Logger.getLogger(this.getClass().getName());

	@Inject
	private TaskService taskservice;
	
	
	@Deployment
	public static WebArchive createDeployment() {

		return ShrinkWrap
				.create(WebArchive.class, "todo-test.war")
				.addClass(Config.class)
				.addClass(Task.class)
				.addClass(TaskService.class)
				.addAsResource("jgroups-cluster-config.xml")
				.addAsWebInfResource(new File("src/main/webapp/WEB-INF/jboss-deployment-structure.xml"))
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	@Test
	@InSequence(1)
	public void should_be_deployed() {
		Assert.assertNotNull(taskservice);
	}

	@Test
	@InSequence(2)
	public void testRetrivingTasks() {
		Collection<Task> tasks = taskservice.findAll();
		Assert.assertNotNull(tasks);
	}

	@Test
	@InSequence(3)
	public void testInsertTask() {
		int orgsize = taskservice.findAll().size();
		Task task = new Task();
		task.setTitle("This is a test task");
		task.setCreatedOn(new Date());
		taskservice.insert(task);
		Assert.assertEquals(orgsize+1, taskservice.findAll().size());
		
		taskservice.delete(task);
		Assert.assertEquals(orgsize, taskservice.findAll().size());
	}

	@Test
	@InSequence(4)
	public void testUpdateTask() {
		int orgsize = taskservice.findAll().size();
		Task task = new Task();
		task.setTitle("This is the second test task");
		task.setCreatedOn(new Date());
		taskservice.insert(task);

		log.info("###### Inserted task with id " + task.getId());
		task.setDone(true);
		task.setCompletedOn(new Date());
		taskservice.update(task);
		Assert.assertEquals(orgsize+1, taskservice.findAll().size());
		
		for (Task listTask : taskservice.findAll()) {
			if("This is the second test task".equals(listTask.getTitle())) {
				log.info("### task =" + listTask.getTitle());
				Assert.assertNotNull(listTask.getCompletedOn());
				Assert.assertEquals(true,listTask.isDone());
				taskservice.delete(listTask);
			}
		}
		Assert.assertEquals(orgsize, taskservice.findAll().size());
	}
	
	@Test
	@InSequence(5)
	public void testFilterTask() {
		Task t1 = generateTestTasks("Sell EAP to customer A", false);
		Task t2 = generateTestTasks("Get FeedBack from EAP customers", false);
		Task t3 = generateTestTasks("Get FeedBack from JDG custoers", true);
		Task t4 = generateTestTasks("Sell JDG to customer B", false);
		Task t5 = generateTestTasks("Pickup kids from daycare", false);
		
		Collection<Task> tasks = taskservice.filter("EAP");
		Assert.assertEquals(2, tasks.size());
		tasks = taskservice.filter("SELL");
		Assert.assertEquals(2, tasks.size());
		tasks = taskservice.filter("FeedBack");
		Assert.assertEquals(2, tasks.size());
		
		taskservice.delete(t1);
		taskservice.delete(t2);
		taskservice.delete(t3);
		taskservice.delete(t4);
		taskservice.delete(t5);
	}

	private Task generateTestTasks(String title, boolean done) {
		Task task = new Task();
		task.setTitle(title);
		if(done) {
			task.setCompletedOn(new Date());
			task.setDone(true);
		}
		taskservice.insert(task);
		return task;
	}
	
	
	
}
