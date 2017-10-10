function [results, conc_grid, eluent_grid, x, conc_final] =  craig4(parameters, square, movie, isVolumeReal, recursedResults)
% Parameter Matrix: (compound_no,C,N,tm,tmax,length,flow,sample_phi,mobile_phi,gradient_time,grad_init_phi,grad_final_phi,td_pump,loop_volume,perc_fill,left_or_right,smooth_rank)
%% UNITS:
    % compound_no: analyte index (integer)         
    % C: concentration of sample (mass/volume)
    % N: number of plates (integer)          
    % tm: minutes                         
    % tmax: maximum time of the simulation(min) 
    % length: column length(centimeters)        
    % flow: mL/min 
    % sample_phi: eluent strength of the sample in percent(0-100)
    % mobile_phi: eluent strength of the mobile phase in percent(0-100)
    % tg: gradient length (minutes)
    % grad_init_phi: initial eluent composition in the gradient in percent(0-100)
    % grad_final_phi: final eluent composition in the gradient in percent(0-100)
    % td_pump: gradient delay time of the pump (min)
    % loop_volume: sample loop volume - the options are 0.4, 15, 20, 40, 60,
    %           and 80 uL
    % perc_fill: percent filling of the loop - the options are 25%, 50%, 75%,
    %           or 100%
    % rank = non-negative integer that controls smoothing of the gradient 
    % leftOrRight: one or two
    % profile: this is a variable that holds the injection loop profiles (from
    % measurements); when using 0.4 uL loop, profile04.mat must be loaded
    % prior, otherwise profile.mat must be loaded prior.

%% NOTES:
    %  Added cubic spline precalculations and then incorporated linear
    %  interpolation for points in between.
    %
    %  Updated by DS 09/18/2014 with compounds at 40, 60, 80 C
    %
    %  10/01/2014 - added a variable 'loss' that indicates what fraction of
    %  sample is not in the loop because of loop loss. This impacts the solvent
    %  profile, and the peak area. The value of 'loss' is on a percent scale
    %  from 0-100.
    %
    %  1/18/2017 - added pseudo-movie functionality within the propagation loop
    %  where the graphs are plotted as they are calculated.
    %
    %  1/18/2017 - Split from craig4_validation 
    %            - Set a calculation for predicted retention time for isocratic
    %            conditions
    %
    %  1/25/2017 - Implemented movie and figure-producing features and
    %  condensed the parameters to a single 
    %            - Switched to using previous matrix (M1) for propagation analyte equation
    %    
    %  2/12/2017 - Implemented an option to toggle real vs. assumed loop
    %               volumes (i.e., isVolumeReal).
    
tic % script timer

if nargin == 0
    % Default values when no variables are provided
    compound_no = 24; % compound ID to assign appropriate k-calculation variables
    C = 2.5;         % analyte concentration (arbitrary units)
    N = 100;        % column plate number
    tm = 0.022;      % column dead time (min)
    tmax = 0.3;      % maximum time in the simulation (min)
    length = 3;      % column length (cm)
    flow = 2.5;      % flow (ml/min)
    sample_phi = 10;     % eluent strength of the sample (0-100% scale)
    mobile_phi = 10;     % eluent strength of the mobile phase outside of the gradient time (0-100% scale)
    gradient_time = 0.25;       % gradient time (min)
    gradient_init_phi = 10;         % initial eluent composition in the gradient (0-100% scale)
    gradient_final_phi = 65;         % final eluent composition in the gradient (0-100%)
    loop_volume = 20;   % volume of the loop (mL)
    td_pump = 0.027; % gradient delay time of the pump (min) 
    perc_fill = 75;   % how much the loop is filled (0-100%)
    left_or_right = 1;
    smooth_rank = 0;
elseif nargin >= 1
    % Argument loading if only a matrix of arguments is provided.
    args = parameters;
    [compound_no,C,N,tm,tmax,length,flow,sample_phi,mobile_phi,gradient_time,gradient_init_phi,gradient_final_phi,td_pump,loop_volume,perc_fill,left_or_right,smooth_rank] = deal(args(1),args(2),args(3),args(4),args(5),args(6),args(7),args(8),args(9),args(10),args(11),args(12),args(13),args(14),args(15),args(16),args(17));
    
    % Auxiliary features are inactivated by default
    switch nargin
        case 1
            square = 0;
            movie = 0;
            isVolumeReal = 0;
        case 2
            movie = 0;
            isVolumeReal = 0;
        case 3
            isVolumeReal = 0;
    end
    noOfSimulations = size(parameters,2);
    if noOfSimulations > 1
        parameters(:,1:noOfSimulations-1)
       recursedResults(:,size(recursedResults,2)) = craig4(parameters(:,1:noOfSimulations-1),square,movie,isVolumeReal,recursedResults);
    end
end

%% Parameter Parsing
    if left_or_right ~= 1 && left_or_right ~= 2 && loop_volume ~= 0.4; error('Please input a 1 (left) or 2 (right) for your 17th variable(leftOrRight)');end
    if 0 > C; error('Please input a nonnegative concentration(C), inclusively');end
    if 0 > smooth_rank; error('Please input a nonnegative integer for rank');end
    if 2 >= N; error('Not enough plates!'); end;
    if 0 > sample_phi || sample_phi > 100; error('Please input a number for sample organic concentration between 0 and 100, inclusively');end
    if 0 > mobile_phi || mobile_phi > 100; error('Please input a number for mobile phase organic concentration between 0 and 100, inclusively');end
    if 0 > gradient_init_phi || gradient_init_phi > 100; error('Please input a number for initial gradient organic concentration between 0 and 100, inclusively');end
    if 0 > gradient_final_phi || gradient_final_phi > 100; error('Please input a number for final gradient organic concentration between 0 and 100, inclusively');end
    if loop_volume ~= 15 && loop_volume ~= 20 && loop_volume ~= 40 && loop_volume ~= 60 && loop_volume ~= 80 && loop_volume ~= 0.4
        error('Please use a loop size of 0.4, 15, 20, 40, 60, or 80 µL'); end
    if perc_fill ~= 25 && perc_fill ~= 50 && perc_fill ~= 75 && perc_fill ~= 100
        error('Please use a percent fill of 25, 50, 75, or 100'); end

%% A and B Variables or kProfile Selector for Propagation Equations
% Compounds 1-5 are for data from Betty; 6-15 are from Klaus; 15< are cubic
% spline interpolations; 24-35 are from Stephan_2.de; and 36-40 are from Betty; 24-40 are based on Neue-Kuss fits using koo
% compounds 41-55 are for data at 40 C collected in second dimension at WAD;
% compounds 56-70 are for data at 60 C collected in second dimension at WAD;
% compounds 71-85 are for data at 80 C collected in second dimension at WAD;
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    if compound_no <= 15
        %     (1)Toluene  (2)AB2  (3)AB3  (4)AB4   (5)AB5  (6)AB1   (7)AB2   (8)AB3  (9)AB4   (10)AB5  (11)BA  (12)NA3   (13)NA4  (14)NA5  (15)NA6 
        rackA=[ -3.315    -3.825  -4.37   -4.798   -5.358  -3.373   -3.948   -4.489   -4.917   -5.496   -1.81   -1.68    -2.090   -3.045    -3.734]; 
        rackB=[ -0.7006   -0.5226 -0.333  -0.093   0.1151  -0.7726  -0.6306  -0.421   -0.182   0.0197   -1.885  -1.269   -1.102   -1.072    -0.930]; 
        A = rackA(compound_no);
        B = rackB(compound_no);
    %% ACN-vs-k' grid for linear interpolation of k' values based on ACN
    elseif compound_no >= 16 && compound_no <= 18 
        %      (16)BA (17)NA3 (18)NA4  %% ACN
        rack = [0.175  0.259   0.339   %% 90
                0.197  0.296   0.413   %% 80
                0.266  0.458   0.673   %% 70
                0.384  0.717   1.118   %% 60
                0.571  1.135   1.925   %% 50        % k' values for compounds based on %ACN
                0.906  1.842   3.506   %% 40
                1.564  3.011   6.727   %% 30
                3.195  4.867   13.19   %% 20
                7.892  8.620   26.177];%% 10
        kProfilePre = rack(9*(compound_no-16)+1:9*(compound_no-16)+9);
        kMatrixIndex = 1;
        ACN_SimpleProfile = 90:-10:10;
        ACN_splinedProfile = 99:-.01:.01;
        kProfile = zeros(1,9900);
        for i = ACN_splinedProfile % defines more k'-vs-ACN points for kProfile (i.e. cubic spline interpolation)
            if mod(i,10) ~= 0
                kProfile(1,kMatrixIndex) = spline(ACN_SimpleProfile,kProfilePre,i);
            else
                kProfile(1,kMatrixIndex) = kProfilePre((100-i)/10);
            end
            kMatrixIndex = kMatrixIndex + 1;
        end   
    %% Neue-Kuss Parameters
    else  %%%%         BA     |    NA3    |    NA4    |   NA5     |    NA5    %%   DEtF   |    BzAlc   |   PB1     |    PB2    |    PB3     |   PB4     |    AP2    |   AP4     |    AP5    |    AP6     |   AP7     |    AP8  %%   AB1     |    AB2    |    AB3     |   AB4     |    AB5   %% DiEtF (40) |   BzAlc    |    PB1     |     PB2     |      PB3      |      PB4     |      AcTan     |      AP2     |      AP3     |      AP4     |      BzPh     |      AP5     |      AP6     |      AP7     |      AP8      %%     DiEtF (60)  |      BzAlc     |      PB1     |      PB2     |      PB3     |      PB4     |      AcTan     |      AP2     |      AP3     |      AP4     |      BzPh     |      AP5     |      AP6     |      AP7     |      AP8      |      DiEtF (80)  |      BzAlc     |      PB1     |      PB2     |      PB3     |      PB4     |      AcTan     |      AP2     |      AP3     |      AP4     |      BzPh     |      AP5     |      AP6     |      AP7     |      AP8      %%%
        kooRack = [25.29377789 16.35118179 64.18279709 430.362296  6685.649662     22.93       37.34      172.5         438        3182.2     10833.5       139.6      2047.6      11010.9     14715.9     17351.3      44937.1    1011         5314        6203         8004        9262      21.66719719 33.21907677   161.5971167  542.8447092    2670.019022    15420.60172    40.45347591      118.9527058     442.3441606    2390.067353    11543.18759    5068.231496     29035.46966    2425.102946     9267.643579       16.5327254        29.16498614      116.6343717    334.8855737   1025.360716    4857.713345    26.62415383     83.59331498   277.4057378      1274.081429    4707.806366     3792.686141     9819.910196  13804.77453    36761.67309       14.83321677       19.78142514    61.43819539   196.9149498   591.9478976      1537.638515    18.23162245      58.23217071   163.9536104     718.6227105    1860.533171      2288.079365   5897.558477   25498.47971     33595.57879];
        aRack   = [1.313609009 0.379440732 0.522617941 0.891421725 1.530972716     2.772       1.903       1.955       1.432       1.839       1.694        1.371      1.239       1.354       0.9757       0.7888      0.7444     1.0438       1.2379      0.9237       0.6486     0.4897     2.93156734  1.856384667   2.042045881  1.768913564    1.848862768    1.955364254    1.697752634      1.206934653     1.145474184    1.362004987    1.683698169    1.109075876     1.275670137    0.168001411     0.293207533       2.466012537       2.126990707      2.33307379      1.829813245    1.597795636   1.683314423  1.508594798       1.13603285     1.049317377    1.243903947    1.518527909     1.164508704     1.044114467    0.84012421    0.818844779        2.678286616      1.954932674     2.154341067  1.833044854    1.584803145       1.400606645    1.493700224      1.168137465  0.939358403     1.191857756    1.340784605    1.130404469     1.058035798     1.174680913   0.946142462];
        BRack   = [15.97840029 7.219685005 10.01138377 16.29813817 29.23124268     27.36       20.96       26.98       24.23       33.53       34.57        18.64      22.73       27.68       23.03        20.26         21       18.59        23.97       19.95        16.82      14.71      27.61990036 19.9087939    27.05176956  27.71889278    32.67275343    38.73398782    20.15852346      16.75453221     18.56267256    24.25850157    32.30750641    22.6308733      28.15988951    10.37943868     13.08773012      23.21651206       21.43669833      28.53763753     26.96891965   27.2427605    32.00630252    17.67456126    15.6040836    16.88885683    21.72113496     28.09632256      22.91157846      23.13177482   20.7738549    21.92045138       24.29752464        19.21897283    25.15114132     25.69499443   25.92551877      25.89259711    16.69692423      15.19710219   15.09692377    20.13238339    24.03023892    21.79251196    22.57262448    26.81461568    23.88649895];
        %%%%%%         19     |    20     |    21     |    22     |    23     %%   24     |    25      |    26     |    27     |    28     |    29     |   30      |    31     |     32    |    33      |    34      |    35   %%     36    |     37    |     38     |    39     |    40    %%     41     |     42     |     43     |     44      |     45        |    46        |      47        |     48       |      49      |      50      |      51       |      52      |      53      |      54      |      55       %%    56            |     57        |     58        |    59         |    60        |    61       |    62          |    63        |    64        |    65        |    66        |    67         |    68        |    69       |    70          |    71           |    72          |    73        |    74         |    75        |    76        |    77         |    78         |    79        |    80        |    81        |    82        |    83         |    84        |    85         %%%
        k00 = kooRack(compound_no - 18); %coefficients for Neue-Kuss fits using koo
        a = aRack(compound_no - 18); %S2
        B = BRack(compound_no - 18); %S1
    end
    
    %% Set up
    if isVolumeReal
       if loop_volume == 15; tempVolume = 13.7; end;
       if loop_volume == 20; tempVolume = 22.7; end;
       if loop_volume == 40; tempVolume = 45.5; end;
       if loop_volume == 60; tempVolume = 68.8; end;
       if loop_volume == 80; tempVolume = 83.3; end;
       input = tempVolume * perc_fill/100;
       else
            if loop_volume == .4 % To account for craig4_validation04
                input = loop_volume;
            else
            input=loop_volume*perc_fill/100; %C alculates input volume(uL)
            end
    end
    Vm=flow*tm; %Calculates column volume (mL)
    dz=length/N; %Calculates plate height (cm)
    dt=tm/N; %Calculates time increment (min) (i.e. dt = dz*tm/length)
    zsteps=floor(length/dz); %equivalent to N %Calculates number of plates
    tsteps=floor(tmax/dt); %Calculates number of time steps in the simulation
    %velocity = dz/dt; %Calculates eluent velocity in cm/s
    plateVelocity = N/(tm*60); %Calculates eluent velocity in plates/s
    dv = Vm*1000/N; %Calculates volume per plate (uL)
    dvT = flow*tm*1000/tsteps; %Calculates volume per tstep (uL)
    NinputV=floor(input/dv); %Calculates number of plates in which samples is introduced initially
    NinputT_loop = floor((loop_volume/dv)/(plateVelocity*dt*60)); %Calculates the number of time steps over which the analyte passes the loop
    NinputT = floor(NinputV/(plateVelocity*dt*60)); %Calculates the number of time steps over which the analyte enters the column
    Ntd=floor(td_pump/dt); %Calculates number of time steps in the pump delay time
    loss = 0; %Percent loss of volume due to dispersion towards the outside of injection loop
    if perc_fill == 100 && loop_volume ~= 0.4
        loss = 7; % 7 percent of volume is lost when filled 100%
    end;
    tgsteps=floor(gradient_time/dt); %Calculates gradient length
    grad=gradient_final_phi-gradient_init_phi; %Calculates gradient height
    slope=grad/tgsteps; %Calculates the slope of the gradient
    conc_final=zeros(1,tsteps); %Preallocates final chromatogram at column outlet
    eluent_final=zeros(1,tsteps); %Preallocates final solvent profile at column outlet
    conc_grid = zeros(1,tsteps); %Preallocates whole time vs. length matrix for analyte
    eluent_grid = zeros(1,tsteps); %Preallocates whole time vs. length matrix for mobile phase
    kp = zeros(1,tsteps);   % Preallocates an array to hold all elution retention factors at each time step 
    
    % Auxiliary features
    Cmax = 0; % Used to track the maximum point for the axes
    Mmax = 0; % in the movie
    % Delay for the square profile that occurs in the tubing before the analyte injection
    %delay = floor(.008/dt);
    delay = 1;
    
    %% Establish injection profile
    % profile is a variable holding injection profiles, where each column vector
    % holds the y signal for time from 0 to 0.5 min.
    
    % Algorithm to determine the index for the correct injection profile in 'profile.mat'
    if loop_volume ~= .4
        load('inj_profiles.mat','inj_profiles');
        index = 0;
        if loop_volume ~= 15 % Divides indices into ten compounds a set.
            index = loop_volume/2;
        end
        index = index + 2*(perc_fill/25);
        if perc_fill == 200
            index = index - 6;
        end
        index = index - (left_or_right - 1);
    else
        load('inj_profiles.mat','profile_400nL');
        index = 1;
        inj_profiles = profile_400nL;
    end
    
    % Processes the correct injection profile by scaling it with tmax
    injProfTime = 0.5;
    profileLength = size(inj_profiles,1);
    profileTrunc = round(profileLength*tmax/injProfTime);
    tempTime = 0:tmax/profileTrunc:tmax;
    tempProf = zeros(size(tempTime));
    if tmax > injProfTime
        profileTrunc = profileLength;
    end
    tempProf(1:profileTrunc) = inj_profiles(1:profileTrunc,index);
    tempProf(profileTrunc+1) = inj_profiles(profileTrunc,index);
    timevec = dt:dt:tmax;
    tempProfInt = interp1(tempTime,tempProf,timevec);
    samProf = tempProfInt;
    
    %% Square sample profile construction
    if square
        samProf = zeros(1,tsteps);
        for i = delay:delay + NinputT
            samProf(1,i) = 1;
        end
    end
    
    %% Injection Profile Shifts
    %% Horizontal sample profile correctional shift
    if loop_volume == .4
        injProDelay = 0;
    else
        injProDelay = .04055;
    end
    if square % If using a square profile, set no profile delay.
        injProDelay = 0;
    end
    NshiftT = floor(injProDelay/dt);
    samProfShift = zeros(1,tsteps-NshiftT);
    for i = 1:tsteps-NshiftT %Shifts the profile .04055 minutes to the left
        samProfShift(1,i) = samProf(1,i + NshiftT);
    end
    
    % Vertical sample profile calibration shift
    diffZer = mean(samProfShift(1,floor(tsteps/2):tsteps-NshiftT)); %Determines how far from zero the baseline is
    samProfShift = samProfShift - diffZer; %Corrects the baseline to 0
    samProfShift(tsteps) = 0; %Appends zeros to end of matrix to match previous size
    samProfArea = trapz(samProfShift(1,:));
    
    %% Establishes the %ACN vs. time profile
    roughGradient = ones(1,tsteps) * mobile_phi; %Preallocates the solvent composition vector
    %plugArea = abs(NinputT*(sample-mobile)); %calculates what the solvent area should be
    for i = 1:tsteps; %Loop that fills the vector with either the sample solvent, or the solvent gradient delivered from the pump
        if i>floor(NinputT_loop+Ntd) && i<=floor(tgsteps+NinputT_loop+Ntd) %Fills the gradient slope
            roughGradient(1,i) = slope*(i-(NinputT_loop+Ntd))+gradient_init_phi; % y = mx + b
        end
    end
    mGradient=roughGradient(1:tsteps)+(NinputT*(sample_phi-mobile_phi))*((100-loss)/100) *samProfShift(1:tsteps)/samProfArea;
    
    %% Smoothes the %ACN vs. time profile
    if smooth_rank ~= 0
        smoothGradient = zeros(1,tsteps); %Preallocates the smoothGradient vector
        smoothGradient(1,floor(tgsteps/2+NinputT+Ntd)+1:tsteps) = mGradient(1,floor(tgsteps/2+NinputT+Ntd)+1:tsteps);
        for i = 1:floor(tgsteps/2+NinputT+Ntd) %Smooths the beginning of the profile using local means
            if i <= smooth_rank %reevaluates points at the beginning of the solvent profile where the mean range is cut off
                smoothGradient(1,i) = mean(mGradient(1,1:i+smooth_rank));
            elseif i > smooth_rank && i < tsteps - smooth_rank %reevaluates a point based on the mean of adjacent values surrounding it
                smoothGradient(1,i) = mean(mGradient(1,i-smooth_rank:i+smooth_rank)); %the size of the mean value range depends on variable 'rank'
            else %reevaluates points at the end of the solvent profile where the mean range is cut off
                smoothGradient(1,i) = mean(mGradient(1,i-smooth_rank:tsteps));
            end
        end
    else
        smoothGradient = mGradient; % No need for smoothing if rank is 0
    end
    
    %% Profile Figure Plot
    % Set to inactive by default, this is set to juxtapose +20 square, real and
    % +0 phi solvent profiles on the same plot.
    if 0
        sample_phi = mobile_phi + 20;
        x=0:dt:tmax-dt;
        gradindices = NinputT_loop + Ntd + tgsteps;
        if NinputT_loop + Ntd + tgsteps > tsteps; gradindices = tsteps; end
        square_profile1 = ones(1,NinputT_loop+1) * sample_phi;
        square_profile2 = ones(1,Ntd) * mobile_phi;
        square_profile1(1,NinputT_loop+1) = mobile_phi;
        flat_profile = ones(1,NinputT_loop) * mobile_phi;
        plot(x(1,1:NinputT_loop+1),square_profile1,'g',...
            x(1,1:NinputT_loop+Ntd),smoothGradient(1,1:NinputT_loop+Ntd),'b',...
            x(1,1:NinputT_loop),flat_profile,'k',...
            x(1,NinputT_loop+1:NinputT_loop+Ntd),square_profile2,'g',...
            x(NinputT_loop+Ntd+1:gradindices),smoothGradient(1,NinputT_loop+Ntd+1:gradindices),'r',...
            x(gradindices:tsteps),smoothGradient(gradindices:tsteps),'r',...
            'linewidth',2);
        h = legend('+20 =       , real','+20 =       , square','  +0 =     ')
        h = legend('       = +20, square','       = +20, real','       = +0')
        htext=findobj(get(h,'children'),'type','text');
        set(htext,'fontsize',2,'FontWeight','light');
        xlabel('Time (min)'); ylabel('%B');
        axis([0 tmax 0 100])
        set(gca,'fontsize',18,'FontWeight','bold','FontName','Arial','YTick',0:10:100)
    end
    
    % Preallocates the initial column profiles of the analyte and the eluent
    % through C1 and M1, respectively.
    C1=zeros(zsteps,1);
    M1=ones(zsteps,1)*mobile_phi;
    
    %% Initial time profile set up
    C1(1,1)=((100-loss)/100)*input*C*samProfShift(1)/samProfArea;
    M1(1,1) = smoothGradient(1);
    
    % Solvent Dispersion
    dC=zeros(zsteps,1);
    D0 = dz^2/(dt*2);
    dC(1) = D0*(-M1(1,1) + M1(2,1)); %for first position and time (using difference version of Fick's second law on p.198, dC represents the numerator on the right side of the equation)
    dC(2,1) = D0*(-2*M1(2,1)+M1(1,1)+M1(3,1)); %for positions from second position point to second to last position point
    M1 = M1+(dC*dt/dz^2);
    eluent_final(1,1)=M1(1,1);
    % Sample Dispersion
    ACN = M1(1,1)/100;
    k = k00*(1+a*ACN)^2*exp(-B*ACN/(1+a*ACN));
    D=dz^2/(dt*(2*(k+1)^2));
    %for first position and time (using difference version of Fick's second law on p.198, dC represents the numerator on the right side of the equation)
    dC(1) = D*(-C1(1,1) + C1(2,1));
    dC(2,1) = D*(-2*C1(2,1)+C1(1,1)+C1(3,1)); %for positions from second position point to second to last position point
    C1 = C1+(dC*dt/dz^2);  %solves for Cz,t+1 on the left side of the equation
    conc_grid(1:zsteps,1)=C1;
    eluent_grid(1:zsteps,1)=M1;
    ACN = M1(zsteps,1)/100;
    kp(1,1) = k00*(1+a*ACN)^2*exp(-B*ACN/(1+a*ACN));
    
    %% Propagation Loops
    % One loop for each k calculation type allows for faster looping since
    % there will be fewer if statements with each iteration. Neue-Kuss
    % parameters can only be used.
    if compound_no >= 20
        for m=2:tsteps
            %% Solvent input`
            M2=zeros(zsteps,1);
            for n=2:zsteps;
                M2(n,1)=M1(n-1,1);
            end
            M2(1,1) = smoothGradient(m);
            
            %% Solvent dispersion
            %for first position and time
            % (using difference version of Fick's second law on p.198, dC
            % represents the numerator on the right side of the equation)
            dC(1)=-D0*M2(1,1)+D0*M2(2,1);
            %for positions from second position point to the second to last position point
            for n=2:zsteps-1
                dC(n,1)=-2*D0*M2(n,1)+D0*M2(n-1,1)+D0*M2(n+1,1);
            end
            %for the last position
            dC(zsteps,1)=-2*D0*M2(zsteps,1)+D0*M2(zsteps-1,1)+D0*M1(zsteps,1);
            M2=M2+(dC*dt/dz^2); %solves for Cz,t+1 on the left side of the equation
            
            %% Sample input
            C2=zeros(zsteps,1);
            ACN = M1(1,1)/100;
            k2= k00*(1+a*ACN)^2*exp(-B*ACN/(1+a*ACN));
            C2(1,1)=C1(1,1)*(k2/(k2+1));
            C2(1,1)=((100-loss)/100)*input*C*samProfShift(m)/samProfArea + C2(1,1);
            for n=2:zsteps
                ACN2 = M1(n,1)/100;
                k=k2; % Transfers the previous plate's value to k
                k2= k00*(1+a*ACN2)^2*exp(-B*ACN2/(1+a*ACN2));
                C2(n,1)=C1(n,1)*(k2/(k2+1))+C1(n-1,1)*(1/(k+1));
            end
            kp(1,m) = k2;
            %% Sample dispersion
            ACN = M2(1,1)/100;
            k = k00*(1+a*ACN)^2*exp(-B*ACN/(1+a*ACN));
            D=dz^2/(dt*(2*(k+1)^2));
            dC(1)=-D*C2(1,1)+D*C2(2,1); %for first position and time (using difference version of Fick's second law on p.198, dC represents the numerator on the right side of the equation)
            for n=2:zsteps-1
                ACN = M2(n,1)/100;
                k = k00*(1+a*ACN)^2*exp(-B*ACN/(1+a*ACN));
                D=dz^2/(dt*(2*(k+1)^2));
                dC(n,1)=-2*D*C2(n,1)+D*C2(n-1,1)+D*C2(n+1,1); %for positions from second position point to second to last position point
            end
            ACN = M2(zsteps,1)/100;
            k = k00*(1+a*ACN)^2*exp(-B*ACN/(1+a*ACN));
            D=dz^2/(dt*(2*(k+1)^2));
            dC(zsteps,1)=-2*D*C2(zsteps,1)+D*C2(zsteps-1,1)+D*C1(zsteps,1); %for last position point
            C2=C2+(dC*dt/dz^2); %solves for Cz,t+1 on the left side of the equation
            
            %% Testing Code
            if m==2; conc_grid(1:zsteps,1)=C1; eluent_grid(1:zsteps,1)=M1; end
            conc_grid(1:zsteps,m)=C2; eluent_grid(1:zsteps,m)=M2;
            
            %% Data Transfer
            C1=C2; M1=M2; conc_final(1,m)=C2(zsteps,1); eluent_final(1,m)=M2(zsteps,1);
            %SumTest(1,m)=sum(C2(1:zsteps,1));
            
            %% MOVIE:
            % Set to inactive by default, this plots C2 as it is calculated
            if 0
                if mod(m,10) == 0
                    plot(C2);
                    drawnow
                    m * dt
                end
            end
            % Actively seeks the maximum value of conc_final and eluent_final
            % by tracking the maximum values of C2 and M2 in order to set the
            % appropriate axes in the movie.
            if movie
                if max(C2) > Cmax; Cmax = max(C2); end;
                if max(M2) > Mmax; Mmax = max(M2); end;
            end
        end
    else
        % If compound index points to a compound that does not utilize Neue-Kuss
        % parameters
        error('Use craig4_validation.mat (pre-2017) for non-Neue-Kuess compounds');
    end
    
    
    %% Finds the peak
    CfinalMax = max(conc_final);
    maxI = floor(median(find(conc_final==CfinalMax)));
    
    %% Area Correction
    ACN = eluent_final(maxI)/100;
    if compound_no <= 15;             k = exp(A * log(ACN) + B);
    elseif compound_no > 20;          k = k00*(1+a*ACN)^2*exp(-B*ACN/(1+a*ACN));
    else                      k = interp1(ACN_splinedProfile,kProfile,ACN*100);
    end
    %y=conc_final./(1+kp); %correction for area
    y=conc_final/(1+k); %lohee
    y=y/dvT; %converts units of mass to units of concentration (mass/volume)
    
    %% Retention Time and Height Calculation
    x=0:dt:tmax;
    ymax=max(y);
    retentionTime=x(maxI);
    
    %% Width (at half height and 4.4% height) calculations
    widths = zeros(1,2);
    modifiers = [4.4;50];
    for n = 1:2
        conc_compare = conc_final;
        compareHeight = CfinalMax*(modifiers(n)/100);
        Clogical = conc_compare >= compareHeight;
        timeslice = x(Clogical);
        widths(n) = mean(max(timeslice))-mean(min(timeslice));
    end
    pw4h = widths(1);
    halfWidth = widths(2);
    
    %% Resizes x with y
    tmaxSize=tmax-2*dt;
    while(size(x,2) ~= size(y,2))
        x=dt:dt:tmaxSize;
        tmaxSize = tmaxSize + dt;
    end
    
    %% Moment calculations
    %Zeroth Moment
    zerothMoment = trapz(x,y);
    
    %First Moment (Identical to retention time if peak is symmetrical)
    firstMoment = 0;
    for i = 1:size(x,2)
        firstMoment = firstMoment + (x(i) * y(i));
    end
    firstMoment = firstMoment*dt / zerothMoment;
    
    %Second Moment- Peak width
    secondMoment = 0;
    for i = 1:size(x,2)
        secondMoment = secondMoment + ((x(i)-firstMoment)^2 * y(i));
    end
    secondMoment = secondMoment*dt / zerothMoment;
    
    %% Graphs the chromatograph at the end of the column
    fig1 = figure(compound_no);
    set(fig1,'Position',[300 100 900 300])
    subplot(1,2,1)
    plot(x,y,'b'); title([num2str(input) ' µL injection  ' num2str(sample_phi) '% ACN']);
    xlabel('Time(min)'); ylabel('Mass');
    subplot(1,2,2)
    plot(x,smoothGradient,'r'); title('Solvent Profile'); axis([0 tmax 0 100])
    xlabel('Time(min)'); ylabel('%ACN');
    
    disp(' ');
    toc;
    % If values are not calculated for width, then it is set to 'N/A'
    if halfWidth == 0; halfWidth='N/A'; N = 'N/A';
    else N=5.54*(retentionTime/halfWidth)^2;
    end
    if pw4h == 0; pw4h='N/A';end;
    
    % Output display for MATLAB's 'Command Window'
    disp(['B: ' num2str(B) '  a: ' num2str(a) '  k00: ' num2str(k00)]);
    disp(['Calc. N:  ' num2str(N)]);
    disp(['Area:     ' num2str(trapz(x,y))]);
    disp(['Sum:      ' num2str(sum(y))]);
    disp(['Ret time: ' num2str(retentionTime) ' mins']);
    disp(['Width:    ' num2str(halfWidth)]);
    disp(['Width:    ' num2str(pw4h) ' (at 4% height)']);
    disp(['Height:   ' num2str(ymax)]);
    disp(['k final:  ' num2str(k)]);
    disp(['Zeroth moment: ' num2str(zerothMoment)]);
    disp(['First moment: ' num2str(firstMoment)]);
    disp(['Second moment: ' num2str(secondMoment)]);
    disp(' ');
    results = [retentionTime halfWidth pw4h ymax trapz(x,y) N k zerothMoment firstMoment secondMoment]';

    % Extracts the frames every 'dFrame' iteration and concatenates them into a
    % real-time movie named 'craig4.avi'
    if movie
        dFrame = 5;
        writerObj = VideoWriter('craig4.avi');
        writerObj.FrameRate = tsteps/dFrame/(tmax*60);
        open(writerObj);
        m = 1;
        xz = dz:dz:length;
        while m < tsteps
            fig1 = figure(compound_no);
            set(fig1,'Position',[300 100 900 600])
        subplot(2,2,1)
            plot(xz,conc_grid(:,m)','b',xz(1,zsteps),conc_grid(zsteps,m)','*b'); title('Analyte profile in column');
            axis([0 length 0 Cmax*1.2])
            xlabel('Length (cm)'); ylabel('Mass');
        subplot(2,2,2)
            plot(xz,eluent_grid(:,m)','r',xz(1,1),eluent_grid(1,m)','*r'); title('%B profile in column');
            axis([0 length 0 Mmax*1.2])
            xlabel('Length (cm)'); ylabel('%ACN');
        subplot(2,2,3)
            plot(x,y,x(1,m),y(1,m),'*b'); title('Chromatogram');
            xlabel('Time (min)'); ylabel('Mass');
        subplot(2,2,4)
            plot(x,smoothGradient,'r',x(1,m),smoothGradient(1,m),'*r'); title('Column Inlet %B'); axis([0 tmax 0 100])
            xlabel('Time (min)'); ylabel('%ACN');
            m = m + dFrame; 
            frame = getframe(fig1);
            writeVideo(writerObj,frame);
        end
        close(writerObj)
    end
end