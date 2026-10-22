package com.learning.ai.application.agent;

import com.learning.ai.domain.enums.AgentCategory;
import com.learning.ai.domain.model.AgentExecutionContext;

public interface SpecialistAiAgent {

    AgentCategory supportedCategory();

    String execute(AgentExecutionContext context);
}
