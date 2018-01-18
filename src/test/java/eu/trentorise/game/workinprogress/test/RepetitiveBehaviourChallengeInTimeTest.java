package eu.trentorise.game.workinprogress.test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import eu.trentorise.game.model.ChallengeModel;
import eu.trentorise.game.model.PointConcept;
import eu.trentorise.game.model.PointConceptStateHelperFactory;
import eu.trentorise.game.model.PointConceptStateHelperFactory.PointConceptStateHelper;
import eu.trentorise.game.model.core.GameConcept;
import eu.trentorise.game.services.GameService;
import eu.trentorise.game.services.PlayerService;
import eu.trentorise.game.test.GameTest;

public class RepetitiveBehaviourChallengeInTimeTest extends GameTest{
 
	private static final String GAME = "repetitiveBehaviourChallenge";
	private static final String ACTION = "save_itinerary";
	private static final String PLAYER = "Hermione Granger";
	
	@Autowired
	private GameService gameSrv;

	@Autowired
	private PlayerService playerSrv;
	
	@Autowired
    private PointConceptStateHelperFactory helperFactory;

	private double score = 0;
	
	@Override
	public void initEnv() {
		PointConceptStateHelper h = helperFactory.instanceOf(GAME, "green leaves");
		double  c1Active = 0, 
				c2Active = 0, 
				c1Target = 10,
				c2Target = 6,
				periodTarget1 = 4,
				periodTarget2 = 2,
				bonus1 = 100,
				bonus2 = 200;
		Date moment;
		int sign = 1,
				s1 = 6,
				s2 = 2;
		Random randomGenerator = new Random();
		
		for(int i = 5; i >= 0; i--) {
			moment = Date.from(
	                LocalDate.now().minusDays(i).atStartOfDay(ZoneId.systemDefault()).toInstant());
			
			sign = (randomGenerator.nextInt(100)%2) == 0 ? 1 : -1;
			int periodScore = 10 + (sign*i);
			score += periodScore;
			h.setScoreInTime(moment, score); 
			
			if((s1 >= i) && (periodScore >= c1Target))
				c1Active++;
			if((s2 >= i) && (periodScore >= c2Target))
				c2Active++;
		}
        
		savePlayerState(GAME, PLAYER, Arrays.asList(h.build()));

		// assign a challenge to PLAYER
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("target", c1Target); //min #greanleaves to consider the day active
		data.put("counterName", "green leaves");
		data.put("periodName", "daily");
		data.put("periodTarget", periodTarget1); //min #days active to win the challenge
		data.put("bonusScore", bonus1);
		data.put("bonusPointType", "green leaves");
		playerSrv.assignChallenge(GAME, PLAYER, "repetitiveBehaviour", "repetitiveBehaviourInstance", data, 
				Date.from(LocalDate.now().minusDays(s1).atStartOfDay(ZoneId.systemDefault()).toInstant()),
				Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
		
		//assign another challenge
		data = new HashMap<String, Object>();
		data.put("target", c2Target); //min #greanleaves to consider the day active
		data.put("counterName", "green leaves");
		data.put("periodName", "daily");
		data.put("periodTarget", periodTarget2); //min #days active to win the challenge
		data.put("bonusScore", bonus2);
		data.put("bonusPointType", "green leaves");
		playerSrv.assignChallenge(GAME, PLAYER, "repetitiveBehaviour", "repetitiveBehaviourInstance2", data, 
				Date.from(LocalDate.now().minusDays(s2).atStartOfDay(ZoneId.systemDefault()).toInstant()),
				Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));

		System.out.println("Initial score = " + score);
		score += ((c1Active >= periodTarget1) ? bonus1 : 0) + ((c2Active >= periodTarget2) ? bonus2 : 0);
		System.out.println("Expected score " + score);
		}

	@Override
	public void defineGame() {	
		List<GameConcept> concepts = new ArrayList<GameConcept>();
		PointConcept p = new PointConcept("green leaves");

        Date startDate = Date.from(
                LocalDate.now().minusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant());
        long dayDurationInMillis = 24 * 60 * 60 * 1000;
        p.addPeriod("daily", startDate, dayDurationInMillis);
        concepts.add(p);
        
		defineGameHelper(GAME, Arrays.asList(ACTION), concepts);

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
		assertionPoint(GAME, score, PLAYER, "green leaves");
	}

}
