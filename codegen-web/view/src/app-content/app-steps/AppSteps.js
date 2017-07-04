/**
 * Created by Nicholas Azar on 2017-07-01.
 */

import React, {Component} from 'react';
import {Steps} from 'antd';
import './AppSteps.less';

const Step = Steps.Step;

class AppSteps extends Component {
    render() {
        return (
            <Steps current={this.props.currentStep} style={{maxWidth: "1000px", margin: "30px auto"}}>
                <Step title="Schema" description="Some text explaining what the schema does" />
                <Step title="Config" description="Configuration for the API" />
                <Step title="Generate" description="Verify and Generate" />
            </Steps>
        )
    }
}

export default AppSteps;