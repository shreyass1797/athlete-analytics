import http from 'k6/http';
import { check, sleep } from 'k6';

// --- 1. ENTERPRISE CONFIGURATION ---
export const options = {
    // The Traffic Spike Simulation
    stages: [
        { duration: '10s', target: 50 },  // Ramp up to 50 concurrent athletes over 10s
        { duration: '30s', target: 50 },  // Sustain the heavy load for 30s
        { duration: '10s', target: 0 },   // Cool down back to 0
    ],
    // The Pass/Fail Criteria (CI/CD Pipeline Rules)
    thresholds: {
        // We expect 95% of all requests to complete in under 200 milliseconds
        http_req_duration: ['p(95)<200'], 
        // We expect zero failed requests
        http_req_failed: ['rate==0.00'],  
    },
};

const BASE_URL = 'http://localhost:8080/api/insights';
const TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzaHJleWFzc3NhN0BnbWFpbC5jb20iLCJpYXQiOjE3NzkwOTQ0NDAsImV4cCI6MTc3OTE4MDg0MH0.Of7_dvwPwEIlEHOHCQKBHie0iRDiBTRP-LQPmkrI13Y'; 

export default function () {
    const params = {
        headers: {
            'Authorization': `Bearer ${TOKEN}`,
            'Content-Type': 'application/json',
        },
    };

    // --- 2. TEST THE READ THROUGHPUT (Proves Database Indexing) ---
    // Simulates the athlete opening the dashboard
    let getRes = http.get(`${BASE_URL}/me`, params);
    
    check(getRes, {
        'GET /me is status 200': (r) => r.status === 200,
        'GET /me returned data fast': (r) => r.timings.duration < 200
    });

    // Simulate the athlete spending 1 second reading the screen
    sleep(1); 

    // --- 3. TEST THE WRITE THROUGHPUT ---
    // Simulates the FastAPI engine saving a new insight
    const today = new Date().toISOString().split('T')[0]; // Formats to "YYYY-MM-DD"
    
    const payload = JSON.stringify({
        date: today,
        fatigueScore: 1.15,
        injuryRiskProbability: 0.12,
        trainingRecommendation: "Load is balanced. Proceed with planned field session."
    });

    let postRes = http.post(`${BASE_URL}/log`, payload, params);
    
    check(postRes, {
        'POST /log is status 200': (r) => r.status === 200,
    });

    // Wait 1 second before the user starts the loop again
    sleep(1);
}