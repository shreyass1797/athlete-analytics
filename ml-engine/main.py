from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import pandas as pd
import numpy as np
from datetime import datetime, timedelta
from sklearn.linear_model import LogisticRegression
from typing import List

app = FastAPI(title="Athlete ML Engine")

# --- 1. Pydantic Schemas (Strict Data Validation) ---
class WorkoutEntry(BaseModel):
    date: str
    durationMinutes: int
    rpe: int

class PredictRequest(BaseModel):
    workouts: List[WorkoutEntry]

class InsightResponse(BaseModel):
    date: str
    fatigueScore: float
    injuryRiskProbability: float
    trainingRecommendation: str

# --- 2. The ML Model Setup ---
def initialize_ml_model():
    X_train = np.array([[0.8, 5], [1.0, 7], [1.2, 8], [1.6, 9], [2.0, 10], [0.5, 4]])
    y_train = np.array([0, 0, 0, 1, 1, 0]) 
    model = LogisticRegression()
    model.fit(X_train, y_train)
    return model

injury_model = initialize_ml_model()

# --- 3. The Calculation Engine ---
def calculate_acwr(df: pd.DataFrame) -> tuple[float, float]:
    if df.empty:
        return 1.0, 5.0 
        
    df['date'] = pd.to_datetime(df['date'])
    df = df.sort_values('date')
    df['daily_load'] = df['durationMinutes'] * df['rpe']
    
    today = pd.to_datetime(datetime.today().date())
    
    acute_mask = (df['date'] > today - pd.Timedelta(days=7))
    acute_load = df.loc[acute_mask, 'daily_load'].sum()
    
    chronic_mask = (df['date'] > today - pd.Timedelta(days=28))
    chronic_load_total = df.loc[chronic_mask, 'daily_load'].sum()
    chronic_load_weekly_avg = chronic_load_total / 4 if chronic_load_total > 0 else 1 
    
    acwr = acute_load / chronic_load_weekly_avg if chronic_load_weekly_avg > 0 else 1.0
    recent_max_rpe = df.loc[acute_mask, 'rpe'].max() if not df.loc[acute_mask].empty else 5.0
    
    return round(acwr, 2), recent_max_rpe

# --- 4. The Secure, Stateless Endpoint ---
# Notice: No user_id in the URL! No Java requests!
@app.post("/predict", response_model=InsightResponse)
def process_athlete_data(payload: PredictRequest):
    try:
        # Convert the incoming JSON array directly into a Pandas DataFrame
        df = pd.DataFrame([workout.model_dump() for workout in payload.workouts])
        
        acwr, recent_max_rpe = calculate_acwr(df)
        
        features = np.array([[acwr, recent_max_rpe]])
        injury_prob = injury_model.predict_proba(features)[0][1] 
        injury_prob = round(injury_prob, 3)
        
        if acwr > 1.5 or injury_prob > 0.40:
            rec = "HIGH RISK: Central Nervous System fatigued. Strict rest or light mobility only."
        elif acwr < 0.8:
            rec = "UNDER-TRAINING: Safe to increase volume and explosiveness."
        else:
            rec = "OPTIMAL: You are in the sweet spot. Maintain current programming."

        return {
            "date": datetime.today().date().isoformat(),
            "fatigueScore": acwr,
            "injuryRiskProbability": injury_prob,
            "trainingRecommendation": rec
        }
            
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Engine Error: {str(e)}")