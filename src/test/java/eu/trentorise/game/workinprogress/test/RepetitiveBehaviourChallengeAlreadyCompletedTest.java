package eu.trentorise.game.workinprogress.test;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import eu.trentorise.game.model.ChallengeConcept;
import eu.trentorise.game.model.ChallengeModel;
import eu.trentorise.game.model.PlayerState;
import eu.trentorise.game.model.PointConcept;
import eu.trentorise.game.model.core.GameConcept;
import eu.trentorise.game.services.GameService;
import eu.trentorise.game.services.PlayerService;
import eu.trentorise.game.test.GameTest;

public class RepetitiveBehaviourChallengeAlreadyCompletedTest extends GameTest{

	private static final String GAME = "repetitiveBehaviourChallenge";
	private static final String ACTION = "save_itinerary";
	private static final String PLAYER = "Hermione Granger";
	
	@Autowired
	private GameService gameSrv;

	@Autowired
	private PlayerService playerSrv;

	@Override
	public void initEnv() {

		List<GameConcept> concepts = new ArrayList<GameConcept>();
		PointConcept p = new PointConcept("green leaves");
		
		long dayDurationInMillis = 24 * 60 * 60 * 1000;
	
		Date todayDate = new Date(); // today
		Date tomorrowDate = new Date(); // today
		tomorrowDate.setTime(todayDate.getTime() + dayDurationInMillis);
		Date yesterdayDate = new Date(); // yesterday
		yesterdayDate.setTime(todayDate.getTime() - dayDurationInMillis);
		Date dayBeforeYesterdayDate = new Date(); // the day before yesterday
		dayBeforeYesterdayDate.setTime(yesterdayDate.getTime() - dayDurationInMillis);
		
		p.addPeriod("daily", dayBeforeYesterdayDate, dayDurationInMillis);
		p.increment(0d);
		p.addPeriod("daily", yesterdayDate, dayDurationInMillis);
		p.increment(0d);
		p.addPeriod("daily", todayDate, dayDurationInMillis);
		p.increment(20d);

		concepts.add(p);
		
		// assign a challenge to PLAYER
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("target", 1d); //min #greanleaves to consider the day active
		data.put("counterName", "green leaves");
		data.put("periodName", "daily");
		data.put("periodTarget", 1d); //min #days active to win the challenge
		data.put("bonusScore", 100d);
		data.put("bonusPointType", "green leaves");
		ChallengeConcept cc = playerSrv.assignChallenge(GAME, PLAYER, "repetitiveBehaviour", "repetitiveBehaviourInstance",
				data, dayBeforeYesterdayDate, tomorrowDate);
		
		cc.completed();
		concepts.add(cc);
		
		savePlayerState(GAME, PLAYER, concepts);

	}

	@Override
	public void defineGame() {		
		defineGameHelper(GAME, Arrays.asList(ACTION), new ArrayList<GameConcept>());

		loadClasspathRules(
				GAME,
				Arrays.asList("rules/" + GAME + "/repetitiveBehaviour.drl"));

		// define challenge models
		ChallengeModel model = new ChallengeModel();
		model.setName("repetitiveBehaviour");
		model.setVariables(new HashSet<String>());
		model.getVariables().add("target");
		model.getVariables().add("counterName");
		model.getVariables().add("periodTarget");
		model.getVariables().add("periodName");
		model.getVariables().add("bonusScore");
		model.getVariables().add("bonusPointType");
		gameSrv.saveChallengeModel(GAME, model);

	}

	@Override
	public void defineExecData(List<ExecData> execList) {
		execList.add(new ExecData(GAME, ACTION, PLAYER, new HashMap<>()));	
	}


	@Override
	public void analyzeResult() {
		//Since the challenge was already completed the player doesn't receive other points
		assertionPoint(GAME, 20d, PLAYER, "green leaves");
	}

}
