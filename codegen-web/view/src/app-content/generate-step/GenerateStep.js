/**
 * Created by Nicholas Azar on 2017-07-05.
 */

import React, {Component} from 'react';
import {Row, Col, Button, Icon} from 'antd';
import Highlight from 'react-highlight'

class GenerateStep extends Component {



    render() {
        return (
            <div style={{padding: '20px'}}>
                <Row type="flex" justify="center">
                    <Col xs={24} sm={18} md={14} lg={12} xl={6}>
                        <h2 style={{color: 'rgba(0, 0, 0, 0.5)'}}>Generate</h2>
                        <p>
                            Review the selected uploaded files and click generate
                        </p>
                    </Col>
                </Row>
                <div style={{height: '20px'}}/>
                <Row type="flex" justify="center">
                    <Col span={10}>
                        <Highlight className="prettyprint lang-json">{
                            "Lorem ipsum dolor sit amet, consectetur adipisicing elit. Beatae distinctio ducimus eaque \n" +
                            "eos eum facere fugiat incidunt labore natus omnis quae quibusdam quo, quod recusandae \n" +
                            "repellendus totam, vel veniam vitae.\n" +
                            "Lorem ipsum dolor sit amet, consectetur adipisicing elit. Accusantium adipisci animi \n" +
                            "corporis cupiditate delectus et, eum ipsum itaque iusto laboriosam neque obcaecati optio \n" +
                            "possimus quae saepe, sed sint temporibus tenetur."
                        }
                        </Highlight>
                    </Col>
                    <Col span={2}>
                    </Col>
                    <Col span={10}>
                        <Highlight className="prettyprint lang-json">{
                            "Lorem ipsum dolor sit amet, consectetur adipisicing elit. Beatae distinctio ducimus eaque \n" +
                            "eos eum facere fugiat incidunt labore natus omnis quae quibusdam quo, quod recusandae \n" +
                            "repellendus totam, vel veniam vitae.\n" +
                            "Lorem ipsum dolor sit amet, consectetur adipisicing elit. Accusantium adipisci animi \n" +
                            "corporis cupiditate delectus et, eum ipsum itaque iusto laboriosam neque obcaecati optio \n" +
                            "possimus quae saepe, sed sint temporibus tenetur."
                        }
                        </Highlight>
                    </Col>
                </Row>
                <Row type="flex">
                    <Col span={2} offset={20}>
                        {/*<Button type="primary" shape="circle" className="nav-button next" onClick={this.props.onNextClick}>*/}
                            {/*<Icon type="arrow-right" style={{fontSize: 20, paddingTop: '4px'}}/>*/}
                        {/*</Button>*/}
                    </Col>
                </Row>
            </div>
        )
    }
}

export default GenerateStep;