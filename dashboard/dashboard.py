import streamlit as st
import requests
import pandas as pd
import plotly.express as px
from datetime import date 


# --- Page Configuration ---
st.set_page_config(page_title="Athlete Analytics", page_icon="⚡", layout="wide")

JAVA_API_BASE = "http://localhost:8080/api"

# --- 1. State Management Initialization ---
if 'jwt_token' not in st.session_state:
    st.session_state['jwt_token'] = None
if 'display_name' not in st.session_state:
    st.session_state['display_name'] = None
if 'user_id' not in st.session_state:
    st.session_state['user_id'] = None

# --- 2. API Helper Functions ---
def register(display_name, email, password):
    payload = {
        "displayName": display_name,
        "email": email,
        "password": password
    }
    response = requests.post(f"{JAVA_API_BASE}/auth/register", json=payload)
    return response.status_code == 200 or response.status_code == 201

def login(email, password):
    response = requests.post(f"{JAVA_API_BASE}/auth/login", json={"email": email, "password": password})
    if response.status_code == 200:
        data = response.json()
        st.session_state['jwt_token'] = data['token']
        st.session_state['display_name'] = data['displayName']
        return True
    return False

def fetch_user_profile():
    headers = {"Authorization": f"Bearer {st.session_state['jwt_token']}"}
    response = requests.get(f"{JAVA_API_BASE}/users/me", headers=headers)
    if response.status_code == 200:
        st.session_state['user_id'] = response.json()['id']
        return True
    return False

@st.cache_data(ttl=60)
def fetch_insights(token): 
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(f"{JAVA_API_BASE}/insights/me", headers=headers) 
    if response.status_code == 200:
        data = response.json()
        # If Spring Boot returns a paginated object, extract the list from 'content'
        if isinstance(data, dict) and 'content' in data:
            return data['content']
            
        # If Spring Boot returns a raw list, just return it
        return data 
        
    return None

@st.cache_data(ttl=60)
def fetch_historical_load(token):
    headers = {"Authorization": f"Bearer {token}"}
    load_data = []

    # 1. Get Gym Workouts
    work_res = requests.get(f"{JAVA_API_BASE}/workouts/me?page=0&size=100", headers=headers)
    if work_res.status_code == 200:
        data = work_res.json()
        workouts = data.get('content', data) if isinstance(data, dict) else data
        for w in workouts:
            load_data.append({
                "date": w["date"], 
                "load": w["durationMinutes"] * w["rpe"], 
                "type": w["type"]
            })

    # 2. Get Field Sessions
    field_res = requests.get(f"{JAVA_API_BASE}/sessions/me?page=0&size=100", headers=headers)
    if field_res.status_code == 200:
        data = field_res.json()
        sessions = data.get('content', data) if isinstance(data, dict) else data
        for s in sessions:
            load_data.append({
                "date": s["date"], 
                "load": s["minutesPlayed"] * s["perceivedFitnessLevel"], 
                "type": s["type"]
            })

    return load_data

@st.cache_data(ttl=60)
def fetch_daily_metrics(token):
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(f"{JAVA_API_BASE}/metrics/me?page=0&size=100", headers=headers)
    if response.status_code == 200:
        data = response.json()
        return data.get('content', data) if isinstance(data, dict) else data
    return None

@st.cache_data(ttl=60)
def fetch_raw_workouts(token):
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(f"{JAVA_API_BASE}/workouts/me?page=0&size=100", headers=headers)
    if response.status_code == 200:
        data = response.json()
        return data.get('content', data) if isinstance(data, dict) else data
    return []

@st.cache_data(ttl=60)
def fetch_raw_sessions(token):
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(f"{JAVA_API_BASE}/sessions/me?page=0&size=100", headers=headers)
    if response.status_code == 200:
        data = response.json()
        return data.get('content', data) if isinstance(data, dict) else data
    return []

# --- DATA LOGGING HELPERS ---
def log_workout(token, log_date, w_type, duration, focus, volume, distance, rpe):
    headers = {"Authorization": f"Bearer {token}"}
    payload = {
        "date": log_date.isoformat(), # Formats to "YYYY-MM-DD" for Spring Boot
        "type": w_type,
        "durationMinutes": duration,
        "primaryFocus": focus,
        "volumeLoad": volume,
        "distanceKm": distance,
        "rpe": rpe
    }
    response = requests.post(f"{JAVA_API_BASE}/workouts/log", headers=headers, json=payload)
    return response.status_code == 200

def log_field_session(token, log_date, s_type, duration, fitness, injuries):
    headers = {"Authorization": f"Bearer {token}"}
    payload = {
        "date": log_date.isoformat(),
        "type": s_type, 
        "minutesPlayed": duration,
        "perceivedFitnessLevel": fitness,
        "injuriesNiggles": injuries
    }
    response = requests.post(f"{JAVA_API_BASE}/sessions/log", headers=headers, json=payload)
    return response.status_code == 200

def log_daily_metrics(token, log_date, sleep, weight, rhr, soreness):
    headers = {"Authorization": f"Bearer {token}"}
    payload = {
        "date": log_date.isoformat(),
        "sleepHours": sleep,
        "morningWeightKg": weight,
        "restingHeartRate": rhr,
        "sorenessScore": soreness
    }
    response = requests.post(f"{JAVA_API_BASE}/metrics/log", headers=headers, json=payload)
    return response.status_code == 200



def generate_and_log_insight(token, log_date):
    headers = {"Authorization": f"Bearer {token}"}
    
    formatted_workouts = []

    # --- 1. Fetch Gym Workouts from Java ---
    work_res = requests.get(f"{JAVA_API_BASE}/workouts/me?page=0&size=100", headers=headers)
    if work_res.status_code == 200:
        data = work_res.json()
        workouts = data.get('content', data) if isinstance(data, dict) else data
        for w in workouts:
            formatted_workouts.append({
                "date": w["date"],
                "durationMinutes": w["durationMinutes"],
                "rpe": w["rpe"]
            })

    # --- 2. Fetch Field Sessions from Java ---
    field_res = requests.get(f"{JAVA_API_BASE}/sessions/me?page=0&size=100", headers=headers)
    if field_res.status_code == 200:
        data = field_res.json()
        sessions = data.get('content', data) if isinstance(data, dict) else data
        for s in sessions:
            formatted_workouts.append({
                "date": s["date"],
                "durationMinutes": s["minutesPlayed"],
                "rpe": s["perceivedFitnessLevel"]
            })

    # --- 3. Ask the Stateless FastAPI Engine for the Prediction ---
    fastapi_url = "http://localhost:8000/predict"
    ml_payload = {"workouts": formatted_workouts}

    try:
        ml_response = requests.post(fastapi_url, json=ml_payload)
        
        if ml_response.status_code == 200:
            prediction = ml_response.json()
            
            # --- 4. Push the Real AI Insight back to Java ---
            insight_payload = {
                "date": log_date.isoformat() if hasattr(log_date, 'isoformat') else log_date,
                "fatigueScore": prediction["fatigueScore"],
                "injuryRiskProbability": prediction["injuryRiskProbability"],
                "trainingRecommendation": prediction["trainingRecommendation"]
            }
            
            requests.post(f"{JAVA_API_BASE}/insights/log", headers=headers, json=insight_payload)
        else:
            st.error(f"ML Engine Error: {ml_response.text}")
            
    except requests.exceptions.ConnectionError:
        st.error("⚠️ Connection Error: Is your FastAPI ML Engine running on port 8000?")

def logout():
    st.session_state.clear()
    st.rerun()

# --- 3. View Routing (Login vs Dashboard) ---
if st.session_state['jwt_token'] is None:
    st.title("⚡ Athlete Command Center")
    
    # Toggle between Login and Register
    auth_mode = st.radio("Choose Action", ["Login", "Register"], horizontal=True)

    if auth_mode == "Login":
        st.subheader("Secure Login")
        with st.form("login_form"):
            email = st.text_input("Email")
            password = st.text_input("Password", type="password")
            submitted = st.form_submit_button("Authenticate")
            
            if submitted:
                if login(email, password):
                    if fetch_user_profile():
                        st.success("Authentication successful!")
                        st.rerun()
                else:
                    st.error("Invalid credentials or server offline.")

    else:
        st.subheader("Create New Account")
        with st.form("register_form"):
            new_name = st.text_input("Full Name")
            new_email = st.text_input("Email")
            new_pass = st.text_input("Password", type="password")
            reg_submitted = st.form_submit_button("Register")
            
            if reg_submitted:
                if not new_name or not new_email or not new_pass:
                    st.warning("Please fill in all fields.")
                elif register(new_name, new_email, new_pass):
                    st.success("Account created! You can now switch to Login.")
                else:
                    st.error("Registration failed. Email might already be taken.")

else:
    # ====== DASHBOARD VIEW ======
    # Navbar
    col_nav1, col_nav2 = st.columns([8, 1])
    with col_nav1:
        st.title(f"⚡ Welcome back, {st.session_state['display_name']}")
    with col_nav2:
        if st.button("Logout"):
            logout()

    # ====== 1. SIDEBAR: DATA ENTRY ======
    with st.sidebar:
        st.header("📝 Log Data")
        
        tab_work, tab_field, tab_metrics = st.tabs(["Workout", "Field Session", "Daily Metrics"])
        
        # --- TAB 1: WORKOUT ---
        with tab_work:
            with st.form("workout_form", clear_on_submit=True):
                w_date = st.date_input("Date", value=date.today(), key="w_date")
                w_type = st.selectbox("Workout Type", ["CALISTHENICS", "WEIGHTLIFTING", "CARDIO", "RECOVERY"])
                w_duration = st.number_input("Duration (mins)", min_value=1, value=60)
                w_focus = st.text_input("Primary Focus (e.g., Muscle-up, 5K)")
                
                col1, col2 = st.columns(2)
                with col1:
                    w_volume = st.number_input("Volume/Load", min_value=0, value=0, help="For lifting/calisthenics")
                with col2:
                    w_distance = st.number_input("Distance (km)", min_value=0.0, value=0.0, step=0.1, help="For cardio")
                
                w_rpe = st.slider("RPE (1-10)", 1, 10, 7, help="1 = Easy, 10 = Max Effort")
                
                if st.form_submit_button("Log Workout"):
                    if log_workout(st.session_state['jwt_token'], w_date, w_type, w_duration, w_focus, w_volume, w_distance, w_rpe):
                        
                        generate_and_log_insight(st.session_state['jwt_token'], w_date)
                        
                        st.success("Workout logged successfully!")
                        st.rerun() 
                    else:
                        st.error("Failed to log workout.")

        # --- TAB 2: FIELD SESSION ---
        with tab_field:
            with st.form("field_form", clear_on_submit=True):
                s_date = st.date_input("Date", value=date.today(), key="s_date")
                s_type = st.selectbox("Session Type", ["PRACTICE", "TOURNAMENT", "MATCH"])
                s_duration = st.number_input("Minutes Played", min_value=1, value=90)
                s_fitness = st.slider("Perceived Fitness Level", 1, 10, 8, help="1 = Sluggish, 10 = Flying")
                s_injuries = st.text_area("Injuries / Niggles", placeholder="Describe any physical issues...")
                
                if st.form_submit_button("Log Field Session"):
                    if log_field_session(st.session_state['jwt_token'], s_date, s_type, s_duration, s_fitness, s_injuries):
                        
                        generate_and_log_insight(st.session_state['jwt_token'], s_date)
                        
                        st.success("Field session logged successfully!")
                        st.rerun() # Refresh to show new charts
                    else:
                        st.error("Failed to log field session.")

        # --- TAB 3: DAILY METRICS ---
        with tab_metrics:
            with st.form("metrics_form", clear_on_submit=True):
                m_date = st.date_input("Date", value=date.today(), key="m_date")
                m_sleep = st.number_input("Sleep Hours", min_value=0.0, max_value=24.0, value=8.0, step=0.5)
                m_weight = st.number_input("Morning Weight (kg)", min_value=30.0, max_value=150.0, value=70.0, step=0.1)
                m_rhr = st.number_input("Resting Heart Rate", min_value=30, max_value=120, value=55)
                m_soreness = st.slider("Soreness Score", 1, 10, 3, help="1 = Fresh, 10 = Completely destroyed")
                
                if st.form_submit_button("Log Daily Metrics"):
                    if log_daily_metrics(st.session_state['jwt_token'], m_date, m_sleep, m_weight, m_rhr, m_soreness):
                        st.success("Metrics logged successfully!")
                    else:
                        st.error("Failed to log metrics.")

    # ====== 2. MAIN DASHBOARD: SECURE DATA FETCH ======
    insights_data = fetch_insights(st.session_state['jwt_token'])

    if insights_data and len(insights_data) > 0:
        st.success("Connected to ML insights! Here's your latest analysis:")
        
        df = pd.DataFrame(insights_data)
        df['date'] = pd.to_datetime(df['date'])
        df = df.sort_values('date') 
        latest_insight = df.iloc[-1]
        
        # --- Top Row: Key Metrics ---
        st.markdown("### Today's Status")
        col1, col2, col3 = st.columns(3)
        with col1:
            st.metric(label="Fatigue Score (ACWR)", value=f"{latest_insight['fatigueScore']}", delta="Target: 0.8 - 1.3", delta_color="off")
        with col2:
            risk_pct = latest_insight['injuryRiskProbability'] * 100
            st.metric(label="Injury Risk Probability", value=f"{risk_pct:.1f}%")
        with col3:
            st.info(f"**Recommendation:**\n{latest_insight['trainingRecommendation']}")
            
        st.divider()
        
        # --- Bottom Row: The Analytics Charts ---
        st.markdown("### Historical Load Trends")
        chart_col1, chart_col2 = st.columns(2)
        with chart_col1:
            fig_fatigue = px.line(df, x='date', y='fatigueScore', title='Fatigue Score over Time (ACWR)', markers=True)
            fig_fatigue.add_hline(y=1.5, line_dash="dash", line_color="red", annotation_text="Danger Zone")
            st.plotly_chart(fig_fatigue, use_container_width=True)
        with chart_col2:
            # Re-added the parenthesis here!
            fig_risk = px.bar(df, x='date', y='injuryRiskProbability', title='Injury Risk Timeline', color='injuryRiskProbability', color_continuous_scale='Reds')
            st.plotly_chart(fig_risk, use_container_width=True)
            
        st.divider()
        st.markdown("### Total Daily Workload")
        
        load_history = fetch_historical_load(st.session_state['jwt_token'])
        
        if load_history:
            df_load = pd.DataFrame(load_history)
            df_load['date'] = pd.to_datetime(df_load['date'])
            df_grouped = df_load.groupby(['date', 'type'])['load'].sum().reset_index()
            
            fig_load = px.bar(
                df_grouped, 
                x='date', y='load', color='type', 
                title='Systemic Load (Duration × RPE)',
                labels={'load': 'Load Score', 'date': 'Date', 'type': 'Session Type'}
            )
            # fig_load.add_hline(y=600, line_dash="dot", line_color="orange", annotation_text="Heavy Day Threshold")
            st.plotly_chart(fig_load, use_container_width=True)

        st.divider()
        st.markdown("### 📚 Training History")
        
        with st.expander("📂 View Workout & Field Session Logs"):
            tab_hist_work, tab_hist_field = st.tabs(["Gym Workouts", "Field Sessions"])
            
            # --- Workout History Table ---
            with tab_hist_work:
                raw_workouts = fetch_raw_workouts(st.session_state['jwt_token'])
                if raw_workouts:
                    df_raw_w = pd.DataFrame(raw_workouts)
                    # Formatting for display
                    df_raw_w['date'] = pd.to_datetime(df_raw_w['date'])
                    df_raw_w = df_raw_w.sort_values('date', ascending=False)
                    df_raw_w['date'] = df_raw_w['date'].dt.date
                    st.dataframe(
                        df_raw_w[['date', 'type', 'durationMinutes', 'primaryFocus', 'rpe', 'volumeLoad', 'distanceKm']],
                        use_container_width=True, hide_index=True
                    )
                else:
                    st.info("No gym workouts logged yet.")

            # --- Field Session History Table ---
            with tab_hist_field:
                raw_sessions = fetch_raw_sessions(st.session_state['jwt_token'])
                if raw_sessions:
                    df_raw_s = pd.DataFrame(raw_sessions)
                    df_raw_s['date'] = pd.to_datetime(df_raw_s['date'])
                    df_raw_s = df_raw_s.sort_values('date', ascending=False)
                    df_raw_s['date'] = df_raw_s['date'].dt.date
                    st.dataframe(
                        df_raw_s[['date', 'type', 'minutesPlayed', 'perceivedFitnessLevel', 'injuriesNiggles']],
                        use_container_width=True, hide_index=True
                    )
                else:
                    st.info("No field sessions logged yet.")
                    
        # --- Daily Metrics ---
        st.divider()
        st.markdown("### ☀️ Morning Readiness & Daily Logs")
        
        metrics_history = fetch_daily_metrics(st.session_state['jwt_token'])
        
        if metrics_history:
            df_m = pd.DataFrame(metrics_history)
            df_m['date'] = pd.to_datetime(df_m['date'])
            df_m = df_m.sort_values('date', ascending=False)
            df_m['date'] = df_m['date'].dt.date
            
            # Readiness Chart
            fig_ready = px.line(df_m, x='date', y=['sleepHours', 'sorenessScore'], 
                               title='Recovery Correlation: Sleep vs. Soreness',
                               labels={'value': 'Score / Hours', 'variable': 'Metric'},
                               markers=True)
            st.plotly_chart(fig_ready, use_container_width=True)

            # Raw Data Table
            with st.expander("📂 View Full Metrics History"):
                display_df = df_m[['date', 'sleepHours', 'morningWeightKg', 'restingHeartRate', 'sorenessScore']]
                st.dataframe(display_df, use_container_width=True, hide_index=True)
        else:
            st.info("No daily metrics logged yet. Start tracking your morning vitals in the sidebar!")
            
    # This 'else' matches the 'if insights_data' check above
    else:
        st.warning("No insight data found. Use the sidebar to log a session first!")