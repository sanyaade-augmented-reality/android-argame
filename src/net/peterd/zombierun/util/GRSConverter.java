package net.peterd.zombierun.util;

import edu.fsu.cs.argame.R;
import java.util.ArrayList;

/**
 * The class that converts GRS80 values to Longitude and Latitude and vice versa
 */
public class GRSConverter {

	//  위도,		경도,	 타원체장반경, 편평도(1/299..), 원점의 직교좌표, 원점의 위도와 경도에 가산할값, 축척계수,  결과X,    결과(Y)
	public static ArrayList<Double> GP2TM(double dPhi, double dLam, double dA, double dF1, double dX0, double dY0, double dPhi0, double dLam0, double dOk)
	{
		double dSphi, dSlam, dSphi0, dSlam0, dF;
		double dFe, dDegrad, recdF, dB, dEs, dEbs, dTn;
		double dAp, dBp, dCp, dDp, dEp;
		double dLamT, dS, dC, dT, dEta, dSn, dTmd, dTmd1, dNfn, dXn1;
		double dT1, dT2, dT3, dT4, dT5, dT6, dT7, dT8, dT9;

		//99.12
		//dFe = 500000#
		dFe = dY0 ;     // 원점의 x좌표
		dF = dF1 ;      // 편평도
		// 편평도가 299.1528... 로 입력 되었으면 0.003342....로 바꿈
		if (dF > 1)
			dF = 1 / dF;

		//dOk = 0.9996   // 축척계수, 고정하지 않고 파라미터로 전달받음 99.12

		dDegrad = Math.atan(1.) / 45.;
		dSphi = dPhi * dDegrad ;  //위도를 라디안으로
		dSlam = dLam * dDegrad;
		dSphi0 = dPhi0 * dDegrad ;   //원점의 위도를 라디안으로
		dSlam0 = dLam0 * dDegrad;
		//
		//    *************************************************
		//    *****   DERIVE OTHER ELLIPSOID PARAMTERS    *****
		//    *****         FROM INPUT VARIABLES          *****
		//    *****    A = SEMI-MAJOR AXIS OF ELLIPSOID   *****
		//    ***** RECF = RECIPROCAL OF FLATTENING (1/F) *****
		//    *************************************************
		//
		//
		//     ** SEMI MAJOR AXIS - B **
		recdF = 1. / dF ;                      // ÆíÆòµµ 299.1528...
		dB = dA * (recdF - 1.) / recdF;
		//
		//     ** ECCENTRICITY SQUARED **
		dEs = (dA*dA - dB*dB) / (dA*dA);
		//
		//     ** SECOND ECCENTRICITY SQUARED **
		dEbs = (dA*dA - dB*dB) / (dB*dB);
		//
		//     ** TRUE MERIDIONAL DISTANCE CONSTANTS **
		dTn = (dA - dB) / (dA + dB);
		dAp = dA * (1. - dTn + 5. * (dTn*dTn - Math.pow(dTn,3)) / 4. + 81. * (Math.pow(dTn,4) - Math.pow(dTn, 5)) / 64.);
		dBp = 3. * dA * (dTn - dTn*dTn + 7. * (Math.pow(dTn,3) - Math.pow(dTn,4)) / 8. + 55. * Math.pow(dTn,5) / 64.) / 2.;
		dCp = 15. * dA * (dTn*dTn - Math.pow(dTn,3) + 3. * ( Math.pow(dTn,4) - Math.pow(dTn,5)) / 4.) / 16.;
		dDp = 35. * dA * (Math.pow(dTn,3) - Math.pow(dTn,4) + 11. * Math.pow(dTn,5) / 16.) / 48.;
		dEp = 315. * dA * (Math.pow(dTn,4) - Math.pow(dTn,5)) / 512.;
		//
		//     ***** ZONE - CENTRAL MERIDIAN *****
		//
		//     *** ZONE ***
		//
		//     *** HOLD FIXED IF IFIXZ IS SET TO ONE
		// 99.12 아래 zone 계산 삭제
		//     *** DETERMINE ZONE NUMBER IF IFIXZ IS ZERO
		//    If IFIXZ = 0 Then
		//     ***
		//       IZONE = 31 + Int(dSlam / dDegrad / 6#)
		//
		//         ** ZONE TRAP - AT HEMISPHERE LIMITS **
		//       If IZONE > 60 Then IZONE = 60
		//       If IZONE < 1 Then IZONE = 1
		//    End If
		//
		//     *** CENTRAL MERIDIAN ***
		//    OLAM# = (IZONE * 6 - 183) * dDegrad
		//
		//     *** DELTA LONGITUDE ***
		//     *** DIFFERENCE BETWEEN LONGITUDE AND CENTRAL MERIDIAN ***
		dLamT = dSlam - dSlam0 ;    // 원점에서 떨어진 경도 rad

		//
		// *** 원점에 대한 x 좌표 dNfn을 먼저 구함    99.12
		//     ** TRUE MERIDIONAL DISTANCE **

		dTmd1 = fnSPHTMD(dAp, dBp, dCp, dDp, dEp, dSphi0);
		dNfn = dTmd1 * dOk;

		// ****  구하는 점의 x 좌표를 구함
		//     *** OTHER COMMON TERMS ***
		dS = Math.sin(dSphi);
		dC = Math.cos(dSphi);
		dT = dS / dC;
		dEta = dEbs * dC*dC;
		//
		//     ** RADIUS OF CURVATURE IN PRIME VERTICAL **
		dSn = fnSPHSN(dA, dEs, dSphi);
		//
		//     ** TRUE MERIDIONAL DISTANCE **
		dTmd = fnSPHTMD(dAp, dBp, dCp, dDp, dEp, dSphi);
		//
		//     ***** NORTHING *****
		//
		dT1 = dTmd * dOk;
		dT2 = dSn * dS * dC * dOk / 2.;
		dT3 = dSn * dS * Math.pow(dC, 3.) * dOk * (5. - dT*dT + 9. * dEta + 4. * dEta*dEta) / 24.;
		dT4 = dSn * dS * Math.pow(dC,5) * dOk * (61. - 58. * dT*dT + Math.pow(dT,4) + 270. * dEta - 330. * dT*dT * dEta 
			+ 445. * dEta*dEta + 324. * Math.pow(dEta,3) - 680. * dT*dT * dEta*dEta + 88. * Math.pow(dEta,4)
			- 600. * dT*dT * Math.pow(dEta,3) - 192. * dT*dT * Math.pow(dEta,4)) / 720.;
		dT5 = dSn * dS * Math.pow(dC,7) * dOk * (1385. - 3111. * dT*dT + 543. * Math.pow(dT,4) - Math.pow(dT,6)) / 40320.;
		//
		//     ** FALSE NORTHING **
		//    dNfn = 0#
		//    If Sgn(dSphi) = -1 Then dNfn = 10000000#
		//
		dXn1 = dT1 + dLamT*dLamT * dT2 + Math.pow(dLamT,4) * dT3 + Math.pow(dLamT, 6) * dT4 + Math.pow(dLamT,8) * dT5;
		double dXn = dXn1 - dNfn + dX0; // 원점과의 차이를 구함   99.12


		//
		//     ***** EASTING *****
		//
		dT6 = dSn * dC * dOk;
		dT7 = dSn * Math.pow(dC,3) * dOk * (1. - dT*dT + dEta) / 6.;
		dT8 = dSn * Math.pow(dC,5) * dOk * (5. - 18. * dT*dT + Math.pow(dT,4) + 14. * dEta - 58. * dT*dT * dEta 
			+ 13. * dEta*dEta + 4. * Math.pow(dEta,3) - 64. * dT*dT * dEta*dEta - 24. * dT*dT * Math.pow(dEta,3)) / 120.;
		dT9 = dSn * Math.pow(dC,7) * dOk * (61. - 479. * dT*dT + 179. * Math.pow(dT,4) - Math.pow(dT,6)) / 5040.;
		//
		double dYe = dFe + dLamT * dT6 + Math.pow(dLamT,3) * dT7 + Math.pow(dLamT,5) * dT8 + Math.pow(dLamT,7) * dT9;

		ArrayList<Double> result = new ArrayList<Double>();
		result.add(dYe);
		result.add(dXn);
		return result;

	}

	public static ArrayList<Double> TM2GP(double dXn, double dYe, double dA, double dF1, double dX0, double dY0,
			   double dPhi0, double dLam0, double dOk)
	{
		//'99.12 DIMEMSION
		double dSphi, dSlam, dSphi0, dSlam0, dF;
		double dFe, dDegrad, dRecf, dB, dEs, dEbs, dTn;
		double dAp, dBp, dCp, dDp, dEp;
		double dLamxx, dS, dC, dT, dEta, dSn, dTmd, dTmd1, dNfn, dXn1;
		double dT10, dT11, dT12, dT13, dT14, dT15, dT16, dT17;
		double dDe, dSr, dFtphi;
		int i;


		dF = dF1;
		// 편평도가 299.1528... 로 입력 되었으면 0.003342....로 바꿈
		if (dF > 1)
			dF = 1 / dF;
		//
		dFe = dY0;
		dDegrad = Math.atan(1.) / 45.;
		dSphi0 = dPhi0 * dDegrad;
		dSlam0 = dLam0 * dDegrad;

		//
		//    *************************************************
		//    *****   DERIVE OTHER ELLIPSOID PARAMTERS    *****
		//    *****         FROM INPUT VARIABLES          *****
		//    *****    A = SEMI-MAJOR AXIS OF ELLIPSOID   *****
		//    ***** RECF = RECIPROCAL OF FLATTENING (1/F) *****
		//    *************************************************
		//
		//
		//     ** SEMI MAJOR AXIS - B **
		dRecf = 1. / dF;
		dB = dA * (dRecf - 1.) / dRecf;
		//
		//	** ECCENTRICITY SQUARED **
		dEs = (dA*dA - dB*dB) / (dA*dA);
		//
		//	** SECOND ECCENTRICITY SQUARED **
		dEbs = (dA*dA - dB*dB) / (dB*dB);
		//
		//	** TRUE MERIDIONAL DISTANCE CONSTANTS **
		dTn = (dA - dB) / (dA + dB);
		dAp = dA * (1. - dTn + 5. * (dTn*dTn - Math.pow(dTn,3)) / 4. + 81. * (Math.pow(dTn,4) - Math.pow(dTn,5)) / 64.);
		dBp = 3. * dA * (dTn - dTn*dTn + 7. * (Math.pow(dTn,3) - Math.pow(dTn,4)) / 8. + 55. * Math.pow(dTn,5) / 64.) / 2.;
		dCp = 15. * dA * (dTn*dTn - Math.pow(dTn,3) + 3. * (Math.pow(dTn,4) - Math.pow(dTn,5)) / 4.) / 16.;
		dDp = 35. * dA * (Math.pow(dTn,3) - Math.pow(dTn,4) + 11. * Math.pow(dTn,5) / 16.) / 48.;
		dEp = 315. * dA * (Math.pow(dTn,4) - Math.pow(dTn,5)) / 512.;
		//
		//     *** HEMISPHERE ADJUSTMENT TO FALSE NORTHING & POINT NORTHING ***
		//     *   NORTHERN HEMISPHERE
		//    dNfn = 0.
		//    If Sgn(dXn) < 0 Then
		//      dNfn = 10000000.
		//      dXn = Abs(dXn)
		//    End If
		//
		// *** 원점에 대한 x 좌표 dNfn을 먼저 구함    99.12
		//     ** TRUE MERIDIONAL DISTANCE **
		dTmd1 = fnSPHTMD(dAp, dBp, dCp, dDp, dEp, dSphi0);
		dNfn = dTmd1 * dOk;

		//     ** TRUE MERIDIONAL DISTANCE FOR FOOTPOINT LATITUDE **

		//    dTmd = (dXn - dNfn) / dOk       '99.12
		dXn1 = dXn + dNfn - dX0;
		dTmd = dXn1 / dOk;
		//
		//     ***** FOOTPOINT LATITUDE *****
		//
		//     ** 1ST ESTIMATE **
		dSr = fnSPHSR(dA, dEs, 0.);
		dFtphi = dTmd / dSr;

		//
		//     ******************************************
		//     ** ITERATE TO OBTAIN FOOTPOINT LATITUDE **
		//
		for(i=0; i<5; i++) //For i = 1 To 5
		{
			//     * COMPUTED TRUE MERIDIONAL *
			dT10 = fnSPHTMD(dAp, dBp, dCp, dDp, dEp, dFtphi);
			//
			//     * COMPUTED RADIUS OF CURVATURE IN THE MERIDIAN *
			dSr = fnSPHSR(dA, dEs, dFtphi);
			//
			//     * CORRECTED FOOTPOINT LATITUDE *
			//     * NEW FTPOINT = LAST FTPOINT +(TMDACTUAL -TMDCOMP)/RADIUS
			dFtphi = dFtphi + (dTmd - dT10) / dSr;
		}//Next i

		//
		//     ******************************************
		//
		//     ** RADIUS OF CURVATURE IN THE MERIDIAN **
		dSr = fnSPHSR(dA, dEs, dFtphi);
		//
		//     ** RADIUS OF CURVATURE IN PRIME VERTICAL **
		dSn = fnSPHSN(dA, dEs, dFtphi);
		//
		//     ** OTHER COMMON TERMS **
		dS = Math.sin(dFtphi);
		dC = Math.cos(dFtphi);
		dT = dS / dC;
		dEta = dEbs * dC * dC;
		//
		//     ** DELTA EASTING - DIFFERENCE IN EASTING **
		dDe = dYe - dFe;
		//     *******************************
		//
		//
		//     ***** LATITUDE *****
		//
		dT10 = dT / (2. * dSr * dSn * dOk *dOk);
		dT11 = dT * (5. + 3. * dT*dT + dEta - 4. * dEta*dEta - 9. * dT*dT * dEta) / (24. * dSr * Math.pow(dSn,3) * Math.pow(dOk,4));
		dT12 = dT * (61. + 90. * dT*dT + 46. * dEta + 45. * Math.pow(dT,4) - 252. * dT*dT * dEta - 3. * dEta*dEta 
			+ 100. * Math.pow(dEta,3) - 66. * dT*dT * dEta*dEta - 90. * Math.pow(dT,4) * dEta + 88. * Math.pow(dEta,4) 
			+ 225. * Math.pow(dT,4) * dEta*dEta + 84. * dT*dT * Math.pow(dEta,3) - 192. * dT*dT * Math.pow(dEta,4)) / (720. * dSr * Math.pow(dSn,5) * Math.pow(dOk,6));
		dT13 = dT * (1385. + 3633. * dT*dT + 4095. * Math.pow(dT,4) + 1575. * Math.pow(dT,6)) / (40320. * dSr * Math.pow(dSn,7) * Math.pow(dOk,8));
		//
		//     ** LATITUDE **
		dSphi = dFtphi - dDe*dDe * dT10 + Math.pow(dDe,4) * dT11 - Math.pow(dDe,6) * dT12 + Math.pow(dDe,8) * dT13;
		//
		//     ***** LONGITUDE *****
		//
		dT14 = 1. / (dSn * dC * dOk);
		dT15 = (1. + 2. * dT*dT + dEta) / (6. * Math.pow(dSn,3) * dC * Math.pow(dOk,3));
		dT16 = (5. + 6. * dEta + 28. * dT*dT - 3. * dEta*dEta + 8. * dT*dT * dEta + 24. * Math.pow(dT,4) 
			- 4. * Math.pow(dEta,3) + 4. * dT*dT * dEta*dEta + 24. * dT*dT * Math.pow(dEta,3)) / (120. * Math.pow(dSn,5) * dC * Math.pow(dOk,5));
		dT17 = (61. + 662. * dT*dT + 1320. * Math.pow(dT,4) + 720. * Math.pow(dT,6)) / (5040. * Math.pow(dSn,7) * dC * Math.pow(dOk,7));
		//
		//     ** DIFFERENCE IN LONGITUDE **
		dLamxx = dDe * dT14 - Math.pow(dDe,3) * dT15 + Math.pow(dDe,5) * dT16 - Math.pow(dDe,7) * dT17;
		//
		//     ** CENTRAL MERIDIAN **
		//    olam# = (IZONE * 6 - 183) * DEGRAD#    // 99.12
		//
		//     ** LONGITUDE **
		//    dSlam = olam# + dlam#
		dSlam = dSlam0 + dLamxx;      // 99.12
		// change to degree unit

		double dPhi = dSphi / dDegrad;
		double dLam = dSlam / dDegrad;
		//
		//     *** ACCURACY NOTE ***
		//     *** TERMS T12, T13, T16 & T17 MAY NOT BE NEEDED IN
		//     *** APPLICATIONS REQUIRING LESS ACCURACY
		//
		
		ArrayList<Double> result = new ArrayList<Double>();
		result.add(dPhi);
		result.add(dLam);
		return result;

	}


	public static double fnSPHTMD(double dAp, double dBp, double dCp, double dDp, double dEp, double dSphi)
	{

//	     *** TRUE MERIDIONAL DISTANCE FROM LATITUDE ***
	//
		double dRet = dAp * dSphi - dBp * Math.sin(2. * dSphi) 
				+ dCp * Math.sin(4. * dSphi) - dDp * Math.sin(6. * dSphi) + dEp * Math.sin(8.* dSphi);
		return dRet;
	}


	static double fnSPHSN(double dA, double dEs, double dSphi)
	{

//	     *** RADIUS OF CURVATURE IN THE PRIME VERTICAL FROM LATITUDE ***
	//
		double dRet = dA / Math.sqrt(1. - dEs * Math.sin(dSphi) * Math.sin(dSphi));

		return dRet;
	}

	static double fnSPHSR(double _dA, double _dEs, double _dSphi)
	{

//	    *** RADIUS OF CURVATURE IN THE MERIDIAN FROM LATITUDE ***
	//
		double dRet = _dA * (1. - _dEs) / Math.pow (fnDENOM(_dEs, _dSphi), 3);

		return dRet;
	}

	static double fnDENOM(double _dEs, double _dSphi)
	{

//	     *** RADIUS OF CURVATURE IN THE MERIDIAN FROM LATITUDE ***
	//
		double dRet = Math.sqrt(1. - _dEs * Math.pow(Math.sin(_dSphi),2));

		return dRet;
	}

	/**
	 * Converts longLat value to grs80
	 * @param lon the longitude value to be converted
	 * @param lat the latitude value to be converted
	 * @return an arraylist, index 0 (grs80 x value), 1 (grs80 y value)
	 */
	public static ArrayList<Double> LLtoKTM(double lon, double lat)
	{
		double dPhi = lat;//5;		// ?낅젰媛?North
		double dLam = lon;//128;	// ?낅젰媛?East
		double dA  = 6378137.0;
		double dF1 = 1.0 / 298.257222101;
		double dX0 = 600000.0;		// ?먯젏??吏곴탳醫뚰몴 North
		double dY0 = 200000.0;	// ?먯젏??吏곴탳醫뚰몴 East
		double dPhi0 =  38.0;
		double dLam0 =  127.0;
		double dOk   = 1.0;

		return GP2TM(dPhi, dLam, dA, dF1, dX0, dY0, dPhi0, dLam0, dOk);
	}


	/**
	 * Converts grs80 value to longlat values
	 * @param _dKtmX grs80 x value to be converted
	 * @param _dKtmY grs80 y value to be converted
	 * @return an arraylist, index 0 (latitude), 1 (longitude)
	 */
	public static ArrayList<Double> KTMtoLL (double _dKtmX, double _dKtmY)
	{
		double dA  = 6378137.0;
		double dF1 = 1.0 / 298.257222101;
		double dX0 = 600000.0;		// ?먯젏??吏곴탳醫뚰몴 North
		double dY0 = 200000.0;	// ?먯젏??吏곴탳醫뚰몴 East
		double dPhi0 =  38.0;
		double dLam0 =  127.0;
		double dOk   = 1.0;

		return TM2GP(_dKtmY, _dKtmX, dA, dF1, dX0, dY0, dPhi0, dLam0, dOk);
	}
}