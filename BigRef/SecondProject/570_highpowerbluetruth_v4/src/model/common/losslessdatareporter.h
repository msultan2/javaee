/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue
*/

#ifndef LOSSLESS_DATA_REPORTER_H_
#define LOSSLESS_DATA_REPORTER_H_



namespace Model
{

template <typename T>
class LosslessDataReporter
{
public:

    //! default constructor
    LosslessDataReporter()
    :
    m_dataSet1(),
    m_dataSet2(),
    m_dataSetNumberInUse(1),
    m_dataSetBeingReportedPtr(0),
    m_dataSetInUsePtr(&m_dataSet1)
    {
        //do nothing
    }

    //! destructor
    virtual ~LosslessDataReporter()
    {
        //do nothing
    }

    const T* getDataSetForReport()
    {
        if (m_dataSetBeingReportedPtr==0) //the last data set being reported has been reset
        {
            m_dataSetBeingReportedPtr = m_dataSetInUsePtr;

            if (m_dataSetNumberInUse==1)
            {
                m_dataSetInUsePtr = &m_dataSet2;
                m_dataSetNumberInUse = 2;
            }
            else
            {
                m_dataSetInUsePtr = &m_dataSet1;
                m_dataSetNumberInUse = 1;
            }
        }
        else //there was no reset() after the last getDataSetForReport() call
        {
            *m_dataSetBeingReportedPtr += *m_dataSetInUsePtr;
            m_dataSetInUsePtr->reset();
        }

        return m_dataSetBeingReportedPtr;
    }

    const T* getDataSetInUsePtr() const { return m_dataSetInUsePtr; }

    T* getDataSetInUsePtr() { return m_dataSetInUsePtr; }

    void reset()
    {
        if (m_dataSetBeingReportedPtr != 0)
        {
            m_dataSetBeingReportedPtr->reset();

            m_dataSetBeingReportedPtr = 0;
        }
        //do nothing
    }

protected:

    //! copy constructor. Not implemented
    LosslessDataReporter(const LosslessDataReporter& );
    //! assignment operator. Not implemented
    LosslessDataReporter& operator=(const LosslessDataReporter& );

    T m_dataSet1;
    T m_dataSet2;
    int m_dataSetNumberInUse;
    T* m_dataSetBeingReportedPtr;
    T* m_dataSetInUsePtr;
};

}

#endif //LOSSLESS_DATA_REPORTER_H_
